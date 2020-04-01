/*
 * Copyright (c) 2017-2018 THL A29 Limited, a Tencent company. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the \"License\");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an \"AS IS\" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tencent.cloud.asr.realtime.sdk.model.response;

import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.utils.JacksonUtil;

/**
 * json字符串解析类，多做了一点赋值。
 * 
 * @author iantang
 * @version 1.0
 */
public class VoiceResponseParser {

	public static VoiceResponse parse(String response) {
		VoiceResponse voiceResponse = null;
		if (AsrInternalConfig.isVadRole()) {
			voiceResponse = JacksonUtil.parse(response, VadResponse.class);
		} else {
			voiceResponse = JacksonUtil.parse(response, VoiceResponse.class);
		}
		voiceResponse.setOriginalText(response);
		return voiceResponse;
	}
}
