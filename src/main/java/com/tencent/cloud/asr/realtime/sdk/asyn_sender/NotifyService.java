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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

import com.tencent.cloud.asr.realtime.sdk.cache_handler.FlowHandler;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;
import com.tencent.cloud.asr.realtime.sdk.utils.ServiceSupport;

/**
 * 回复内容通知。提供回复数据的回调服务。
 * 
 * 对于所有注册的Handler，目前暂时只有单线程逐一按Hander的注册顺序提供通知服务。
 * 
 * @author iantang
 * @version 1.0
 */
public class NotifyService extends ServiceSupport {

	/**
	 * 通知队列，回调服务会从此服务中拿走数据回调用户的方法。
	 */
	public BlockingQueue<VoiceResponse> notifyQueue = new LinkedTransferQueue<VoiceResponse>();

	private boolean keepRunning = true;

	private List<FlowHandler> registerList = new ArrayList<FlowHandler>();

	private long serverId;

	public NotifyService(long serviceId) {
		this.serverId = serviceId;
	}

	@Override
	protected void _start() throws Exception {
		while (this.keepRunning) {
			VoiceResponse voiceResponse = this.takeNext();
			for (FlowHandler flowHandler : this.registerList) {
				voiceResponse.getTimeStat().setNotifyDelay(); // 客户处理数据的耗时大于NodeDelay时，NotifyDelay也会变大。
				flowHandler.onUpdate(voiceResponse);
			}
		}
		System.out.println(this.getName() + " stopped, clear the response not notified yet, size: "
				+ this.notifyQueue.size());
		this.notifyQueue.clear();
	}

	@Override
	protected void _stop() throws Exception {
		this.registerList.clear(); // upRegister all handlers
		this.keepRunning = false;
		this.transfer(new VoiceResponse());// 确保服务能停止
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getName() {
		return "NotifyService_" + serverId;
	}

	public void newMessageComming(VoiceResponse voiceResponse) {
		this.transfer(voiceResponse);
	}

	@Override
	public String getDescription() {
		return "Notify client's handler when new response received.";
	}

	public VoiceResponse takeNext() {
		VoiceResponse firstPacket = null;
		try {
			firstPacket = this.notifyQueue.take();
		} catch (InterruptedException e) {
			// e.printStackTrace();
			System.err.println("Notify Service take next response has been Interrupted! will sotp request.");
		}
		return firstPacket;
	}

	public void register(FlowHandler flowHandler) {
		if (!this.registerList.contains(flowHandler))
			this.registerList.add(flowHandler);
	}

	public int getNotifyQueueSize() {
		return this.notifyQueue.size();
	}

	private void transfer(VoiceResponse voiceResponse) {
		try {
			this.notifyQueue.put(voiceResponse);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("Notify Service transfer resonse: '" + voiceResponse.getOriginalText()
					+ "' failed, queue interrupted! ");
		}
	}

}
