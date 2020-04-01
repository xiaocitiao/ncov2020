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

import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.model.response.TimeStat;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;

/**
 * 独立的耗时统计对象，辅助统计请求时长消耗情况。以当前值和平均值来标识。
 * 
 * @author iantang
 * @version 1.0
 */
public class TimeRecoder {

	/**
	 * 当前VAD段落的分片的请求回复总耗时
	 */
	private long preWriteDelayTotal = 0;

	/**
	 * 当前VAD段落的分片的节点总耗时
	 */
	private long preNodeDelayTotal = 0;

	/**
	 * 当前VAD段落的分片回复的总个数，相当于计数器。
	 */
	private long size;

	/**
	 * 添加中间分片的延迟统计，voiceResponse为null也纳入平均值统计中。
	 */
	public void addMiddleDelay(VoiceResponse voiceResponse, long messageTime, long writeTime) {
		long end = System.currentTimeMillis();
		long writeDelay = end - writeTime;
		long nodeDelay = end - messageTime;

		this.size++;
		this.preWriteDelayTotal += writeDelay;
		this.preNodeDelayTotal += nodeDelay;

		if (voiceResponse == null)
			return;
		this.addResponseDelay(voiceResponse, writeDelay, nodeDelay);
		if (AsrInternalConfig.isVadRole() && voiceResponse.containsEndResult()) {
			this.reset();
		}
	}

	/**
	 * 添加结束分片的延迟统计
	 */
	public void addEndDelay(VoiceResponse voiceResponse, long messageTime, long writeTime) {
		long end = System.currentTimeMillis();
		long writeDelay = end - writeTime;
		long nodeDelay = end - messageTime;

		if (voiceResponse != null) {
			this.addResponseDelay(voiceResponse, writeDelay, nodeDelay);
			voiceResponse.getTimeStat().setEndCut(true);
		}
		this.reset(); // Vad版：多执行一次也没关系
	}

	private void addResponseDelay(VoiceResponse voiceResponse, long writeDelay, long nodeDelay) {
		if (voiceResponse == null)
			return;

		// 计算前面的平均耗时
		long preAverageWriteDelay = this.size > 0 ? this.preWriteDelayTotal / this.size : 0;
		long preAverageNodeDelay = this.size > 0 ? this.preNodeDelayTotal / this.size : 0;
		/*System.out.println("One VAD sentence's previous cut size: " + this.size + ", preWriteDelayTotal: "
				+ preWriteDelayTotal + "ms, pre average wrte Delay: " + (this.preWriteDelayTotal / this.size) + "ms.");*/

		// 将结果赋值给voiceResponse
		TimeStat timeStat = voiceResponse.getTimeStat();
		timeStat.setWriteDelay(writeDelay);
		timeStat.setNodeDelay(nodeDelay);
		timeStat.setPreAverageWriteDelay(preAverageWriteDelay);
		timeStat.setPreAverageNodeDelay(preAverageNodeDelay);
	}

	public void reset() {
		this.preWriteDelayTotal = 0;
		this.preNodeDelayTotal = 0;
		this.size = 0;
	}
}
