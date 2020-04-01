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
package com.tencent.cloud.asr.realtime.sdk.cache_handler;

/*import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;*/

import com.tencent.cloud.asr.realtime.sdk.asyn_sender.NotifyService;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;

/**
 * 请求和回复的对应关系即回复内容管理类。
 * 
 * <pre>
 * 之前还包含了：CommingResponseHandler, ResponseManager, MessageRetriever等几个类和相关的功能，现在先行取消。 
 * 如将来需要扩展回复内容的管理和推送等功能，欢迎与iantang交流。
 * </pre>
 * 
 * @author iantang
 * @version 1.0
 */
public class MessageCacheManager {

	private NotifyService notifyService;

	public MessageCacheManager(NotifyService notifyService) {
		this.notifyService = notifyService;
	}

	/**
	 * 当前内存中的Voice返回值Map. Not used yet.
	 */
	/*public Map<String, VoiceResponse> responseMap = new ConcurrentHashMap<String, VoiceResponse>();*/

	/**
	 * 当前内存中的voiceId列表
	 */
	/*private List<String> voiceIdList = new LinkedList<String>();*/

	public void newMessageComming(VoiceResponse voiceResponse) {
		/*String voiceId = voiceResponse.getVoiceId();
		this.responseMap.put(voiceId, voiceResponse);
		this.voiceIdList.add(voiceId);*/

		// 回调服务
		this.notifyService.newMessageComming(voiceResponse);
	}

	public void clear() {
		/*this.voiceIdList.clear();
		this.responseMap.clear();*/
	}
}
