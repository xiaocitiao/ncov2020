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
package com.tencent.cloud.asr.realtime.sdk.asyn_sender;

import java.util.List;

import com.tencent.cloud.asr.realtime.sdk.cache_handler.MessageCacheManager;
import com.tencent.cloud.asr.realtime.sdk.cache_handler.ReceiverCache;
import com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.http.asynchronize.AsynRequestSender;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBytesRequest;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;
/*import com.tencent.cloud.asr.realtime.sdk.utils.DateTimeUtils;*/
import com.tencent.cloud.asr.realtime.sdk.utils.ServiceSupport;

/**
 * 负责从队列中取出数据并不断请求，将回复写入结果集中。
 * 
 * 考虑到End标记的不确定性，以及同一条语音分成了多个数据包对象，为了确保数据连续性，暂时只能用单线程发送Httpq请求。
 * 
 * 如果后续需要换成多线程，则需要新增转发策略模块（相同的voiceId转发到同一条语音中）。
 * 
 * @author iantang
 * @version 1.0
 */
public class RequestService extends ServiceSupport {

	private ReceiverCache receiverCache;
	private MessageCacheManager messageCacheManager;
	private AsynRequestSender asynRequestSender = new AsynRequestSender();
	private long serviceId;

	private boolean keepRunning = true;

	public RequestService(ReceiverCache receiverCache, NotifyService notifyService, long serviceId) {
		this.receiverCache = receiverCache;
		this.messageCacheManager = new MessageCacheManager(notifyService);
		this.serviceId = serviceId;
	}

	@Override
	protected void _start() throws Exception {

		while (this.keepRunning) {
			RasrBytesRequest rasrBytesRequest = this.receiverCache.takeNext();
			if (rasrBytesRequest == null) {
				System.err.println("Transfer queue interrupted, please check error or try restart the service.");
				break;
			}
			/*System.out.println("---------------------------------------------------------------------------\n"
					+ DateTimeUtils.dateTime1.format(System.currentTimeMillis()) + " "
					+ this.getClass().getSimpleName() + " taked message--->"
					+ DateTimeUtils.dateTime1.format(rasrBytesRequest.getMessageTime()));*/

			// 开始发送Http请求
			List<VoiceResponse> list = this.asynRequestSender.send(rasrBytesRequest, this.receiverCache.touchNext());

			// 将全部或(Vad)尾包发给Lisener。
			// Vad版注意：中间断开的句子解析的结果，也需要发给客户。因为目前识别服务器是不会在最后汇总的。
			for (VoiceResponse voiceResponse : list) {
				if (AsrGlobelConfig.NOTIFY_ALL_RESPONSE || voiceResponse.containsEndResult()) {
					this.messageCacheManager.newMessageComming(voiceResponse);
				} else {
					/*System.out.println("Not notify --->" + voiceResponse.getOriginalText());*/
				}
			}
		}
		System.out.println(this.getName() + " stopped.");
	}

	@Override
	protected void _stop() throws Exception {
		this.keepRunning = false;
		this.receiverCache.add(new byte[AsrGlobelConfig.CUT_LENGTH]);// 确保服务能停止
		try {
			Thread.sleep(50); // 确保线程没有卡在takeNext方法上。
		} catch (InterruptedException e) {
			// ignore
		}
		this.messageCacheManager.clear();
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getName() {
		return "RequestService_" + serviceId;
	}

	@Override
	public String getDescription() {
		return "send request to: " + AsrInternalConfig.REAL_ASR_URL;
	}
	
	public AsynRequestSender getAsynRequestSender() {
		return asynRequestSender;
	}

	public VoiceResponse getLastResponse() {
		return this.asynRequestSender.getLastReponse();
	}
}
