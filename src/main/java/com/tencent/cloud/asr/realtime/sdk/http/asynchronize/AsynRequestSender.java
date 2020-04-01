/*
 * Copyright (c) 2017-2018 THL A29 Limited, a Tencent company. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tencent.cloud.asr.realtime.sdk.http.asynchronize;

import static com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig.CUT_LENGTH;

import java.util.ArrayList;
import java.util.List;
import com.tencent.cloud.asr.realtime.sdk.http.base.HttpRequester;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBytesRequest;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;
import com.tencent.cloud.asr.realtime.sdk.utils.ByteUtils;

/**
 * 异步请求功能中的Http请求发送对象。有做一些特别的处理。1.1版本，方案可行，已测试。新方案逻辑相对简单，发送更及时。
 * 
 * <pre>
 * 目标如下：
 * 将请求对象中的数据，拼装或者拆分成等于CUT_LENGTH值的数组，按顺序发出请求，最后一份数据长度小于CUT_LENGTH时，暂存在
 * lastCachedBytes变量中。发送 “voiceEnd”请求时，如果刚好无剩余数据可发，则请求消息体（httpBody）中使用空的字节数组代替。
 * 
 * 处理流程有简化，描述如下：
 * 1. 先判断当前请求是否携带voiceEnd信号，且请求中没有消息体：是则发出最后的回复，本次结束。
 * 2. 常规处理逻辑：先处理消息体（即list）中前面的N-1个数组，拼装和拆分成多个CUT_LENGTH长度的数组，发出请求。
 * 3. 特别处理最后一个数组，余下一份数据。
 * 4. 处理最后一份数据，三选一：发出voiceEnd请求、或者发送当前分片请求、或者存储到lastCachedBytes中。
 * 
 * 另外，本对象在收到回复后，记录了每个分片的请求耗时；标记了是否为最后一个分片；统计了前面N个分片的平均请求耗时。
 * </pre>
 * 
 * @author iantang
 * @version 1.0
 */
public class AsynRequestSender {

	private HttpRequester httpRequester = new HttpRequester();

	/**
	 * 发送voiceEnd请求时，如果无剩余数据可用，则使用此空数组代替，写入HttpBody中。
	 * 
	 * 对于目前8K和16K的语音识别服务器，2的倍数个字节不会报错。将来如有需要，可能需要改成更大的数组。
	 */
	private byte[] EMPTY_BYTES = new byte[4];

	/**
	 * 最后一份未被发出的数组数据
	 */
	private byte[] lastCachedBytes = null;

	/**
	 * 下一个发出的请求所对应的seq号码
	 */
	private int seq = 0;

	/**
	 * 专门做延迟统计的对象
	 */
	private TimeRecoder timeRecoder = new TimeRecoder();

	/**
	 * 当前最后一次收到的回复。
	 */
	private VoiceResponse lastResponse = null;

	private boolean newVoiceComming = false;

	/**
	 * 当前全部回复组成的列表。对于一个请求对象中，可能出现的多个有效的开始、中间、尾包回复，都放到list中，后面通知时再做区分。
	 */
	private List<VoiceResponse> responseList = new ArrayList<VoiceResponse>();

	public List<VoiceResponse> send(RasrBytesRequest rasrBytesRequest, RasrBytesRequest nextRequest) {
		this.responseList.clear(); // 先清空。
		if (rasrBytesRequest.isEndReceived() && rasrBytesRequest.getBytesList().size() == 0) {
			if (this.newVoiceComming) {
				// 发送voiceEnd信号。
				this.sendEndBytes(rasrBytesRequest, this.lastCachedBytes);
			} else {
				// Ignore.通常是因为用户重复调用了ReceiverEntrance.voiceEnd()方法,或者未add数据直接调用voiceEnd()。
				System.out.println("Skip send, no need repeat call voiceEnd(); Or previous error connection.");
			}
			return this.responseList;
		}

		// 正常情况的处理：
		byte[] current = this.lastCachedBytes;
		int size = rasrBytesRequest.getBytesList().size();
		// 常规处理前面n个数组，最后一个除外：
		for (int i = 0; i < size - 1; i++) {
			byte[] message = rasrBytesRequest.getBytesList().get(i);
			if (current == null) {
				current = message;
				continue;
			}
			// 拼接
			current = ByteUtils.concat(current, message);
			// 切成小块发出去
			if (current.length >= CUT_LENGTH) {
				current = this.cutAndSend(rasrBytesRequest, current, true);
			}
		}

		// 特别处理最后一个数组，且不处理完：
		byte[] lastMessage = rasrBytesRequest.getBytesList().get(rasrBytesRequest.getBytesList().size() - 1);
		current = this.contact(current, lastMessage);
		if (current.length > CUT_LENGTH) {
			current = this.cutAndSend(rasrBytesRequest, current, false);
		}

		// 处理剩下的最后一份数据：
		if (rasrBytesRequest.isEndReceived()) {
			this.sendEndBytes(rasrBytesRequest, current);
		} else {
			if (current != null && current.length == CUT_LENGTH)
				current = this.cutAndSend(rasrBytesRequest, current, true);
			this.lastCachedBytes = current;
		}
		return this.responseList;
	}

	/**
	 * 获取当前最后一次收到的回复内容。
	 */
	public VoiceResponse getLastReponse() {
		return lastResponse;
	}

	/**
	 * 重新赋值新的请求对象。
	 */
	public void setHttpRequester(HttpRequester httpRequester) {
		this.httpRequester = null;
		this.httpRequester = httpRequester;
	}

	/**
	 * 将最后一个分片的请求发出去，并获取回复。同时获得本次请求所花时间。
	 */
	private void sendEndBytes(RasrBytesRequest rasrBytesRequest, byte[] endBytes) {
		if (endBytes == null) {
			endBytes = this.EMPTY_BYTES; // 经测试，可以发送内容为空的数组，长度为2的倍数时识别不会出错。
		}
		long writeTime = System.currentTimeMillis();
		VoiceResponse voiceResponse = this.sendRequest(rasrBytesRequest, 1, endBytes);
		if (voiceResponse != null)
			voiceResponse.getTimeStat().setEndCut(true); // 标记为结束分片的回复
		this.timeRecoder.addEndDelay(voiceResponse, rasrBytesRequest.getMessageTime(), writeTime);
		this.checkAddResponse(voiceResponse);

		this.lastCachedBytes = null;
		this.seq = 0;
		this.newVoiceComming = false;
	}

	private byte[] contact(byte[] current, byte[] lastMessage) {
		if (current == null) {
			return lastMessage;
		} else {
			return ByteUtils.concat(current, lastMessage);
		}
	}

	/**
	 * 切分数组并发送请求。所有发出去的数组，都不是最后一个分片。
	 * 
	 * @param canSendLast
	 *            如果最后一块刚好等于CUT_LENGTH时，是否可以发出。如果为false，则即使最后一块的长度刚好等于CUT_LENGTH，也不会切分。
	 * 
	 * @return 返回剩下的数组。如果刚好切分完，则返回null
	 */
	private byte[] cutAndSend(RasrBytesRequest rasrBytesRequest, byte[] current, boolean canSendLast) {
		int posi = 0;
		while (current.length - posi > CUT_LENGTH) {
			sendMiddleCut(rasrBytesRequest, current, posi);
			posi = posi + CUT_LENGTH;
		}
		if (current.length - posi == CUT_LENGTH && canSendLast) {
			sendMiddleCut(rasrBytesRequest, current, posi);
			return null;
		}
		// 返回剩下的最后一块
		return ByteUtils.subBytes(current, posi, current.length - posi);
	}

	private void sendMiddleCut(RasrBytesRequest rasrBytesRequest, byte[] current, int posi) {
		long writeTime = System.currentTimeMillis();
		byte[] cut = ByteUtils.subBytes(current, posi, CUT_LENGTH);
		VoiceResponse voiceResponse = this.sendRequest(rasrBytesRequest, 0, cut);
		this.timeRecoder.addMiddleDelay(voiceResponse, rasrBytesRequest.getMessageTime(), writeTime);
		this.checkAddResponse(voiceResponse);
		this.newVoiceComming = true;
	}

	private void checkAddResponse(VoiceResponse voiceResponse) {
		if (voiceResponse == null)
			return;
		this.lastResponse = voiceResponse;
		this.responseList.add(voiceResponse); // 这里暂时不做尾包区分，直接添加好了，后面再做区分。
	}

	private VoiceResponse sendRequest(RasrBytesRequest rasrBytesRequest, int end, byte[] dataPacket) {
		rasrBytesRequest.updateSeqAndEndFlag(this.seq, end);
		VoiceResponse voiceResponse = this.httpRequester.sendAndParse(rasrBytesRequest, dataPacket);
		if (voiceResponse != null)
			voiceResponse.getTimeStat().setMessageTime(rasrBytesRequest.getMessageTime()); // 传递messageTime
		this.seq++;

		return voiceResponse;
	}
}
