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
package com.tencent.cloud.asr.realtime.sdk.config;

import com.tencent.cloud.asr.realtime.sdk.model.enums.EngineModelType;
import com.tencent.cloud.asr.realtime.sdk.model.enums.ResponseEncode;
import com.tencent.cloud.asr.realtime.sdk.model.enums.VoiceFormat;

/**
 * 用户的个性化配置。可实时修改，也可以仅配置一次后一直使用。
 * 
 * 如果直接采用默认配置，请先确认自身请求的数据是否与默认配置相符。
 * 
 * @author iantang
 * @version 1.0
 */
public class AsrPersonalConfig {

	/**
	 * 返回值编码配置，默认设置为：UTF-8
	 */
	public static ResponseEncode responseEncode = ResponseEncode.UTF_8;

	/**
	 * 引擎类型，应该根据请求的音频格式自行设定。如果不匹配，识别准确率会降低。
	 */
	public static EngineModelType engineModelType = EngineModelType._16k_0;

	/**
	 * 音频流或音频文件的格式。如果不匹配，识别准确率可能会降低。
	 */
	public static VoiceFormat voiceFormat = VoiceFormat.sp;

	
}
