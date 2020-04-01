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
package com.tencent.cloud.asr.realtime.sdk.model.request;

import java.util.ArrayList;
import java.util.List;

import com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig;

public class RasrBytesRequest extends RasrBaseRequest {

	private List<byte[]> bytesList = new ArrayList<byte[]>();
	private int length = 0;

	/**
	 * 是否已经收到结束标志。如果收到，说明本对象中的数组集合是一句完整的话。
	 */
	private boolean endReceived = false;

	private int cursor = 0;

	/** 中间版本用到的一个变量，现已不用。将来如有需要可再启用。 */
	private long firstAddTime;

	private long messageTime;

	public RasrBytesRequest() {
		super();
	}

	/**
	 * 构造函数，以前一个对象为参考，构建一个新对象。达到：保持voiceId的目的。
	 */
	public RasrBytesRequest(RasrBytesRequest previous) {
		super(previous);
	}

	public RasrBytesRequest(byte[] content) {
		super();
		this.firstAddTime = System.currentTimeMillis();
		this.bytesList.add(content);
	}

	public void add(byte[] content) {
		if (this.bytesList.size() == 0)
			this.firstAddTime = System.currentTimeMillis();// 第一次add bytes的时间,将来如有需要可启用它。
		this.bytesList.add(content);
		this.length += content.length;
	}

	public List<byte[]> getBytesList() {
		return bytesList;
	}

	public long getFirstAddTime() {
		return firstAddTime;
	}

	public long getMessageTime() {
		return messageTime;
	}

	public void setMessageTime(long messageTime) {
		this.messageTime = messageTime;
	}

	public void setEndReceived() {
		if (this.firstAddTime == 0 && this.bytesList.size() == 0) {
			this.firstAddTime = System.currentTimeMillis(); // 避免voiceEnd()方法调用且bytesList无数据时firstAddTime为0.
		}
		this.endReceived = true;
	}

	/**
	 * 是否已经收到结束标志。如果为true，说明本对象中的数组集合是一句完整的话。
	 */
	public boolean isEndReceived() {
		return endReceived;
	}

	/**
	 * 获取对象中的下一个数组。如果已全部获取完，则返回Null。
	 * 
	 * 该方法线程不安全。
	 * 
	 * 若本方法同时被多个线程调用，则各线程拿到的都是语音的某个片段，且顺序也丢失了，最终会造成无法拼接出完整的语音来。
	 */
	public byte[] getNext() {
		if (this.cursor >= this.bytesList.size()) {
			return null;
		}
		byte[] bytes = this.bytesList.get(this.cursor);
		this.cursor++;
		return bytes;
	}

	/**
	 * 判断当前所持有的字节数组总长度是否已足够
	 */
	public boolean lengthEnough() {
		return this.length >= AsrGlobelConfig.CUT_LENGTH;
	}

	public boolean containsData() {
		return this.length > 0;
	}
}
