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
package com.tencent.cloud.asr.realtime.sdk.model.enums;

/**
 * 声音类型。mp3仅仅16k_0模型支持，其它的则都能支持。
 * 
 * @author iantang
 * @version 1.0
 */
public enum VoiceFormat {

	wav(1), sp(4), silk(6), mp3(8);

	private int formatId;

	private VoiceFormat(int formatId) {
		this.formatId = formatId;
	}

	public int getFormatId() {
		return formatId;
	}

	public static VoiceFormat parse(int id) {
		switch (id) {
		case 1:
			return wav;
		case 4:
			return sp;
		case 6:
			return silk;
		default:
			return wav;
		}
	}

	public static VoiceFormat parse(String formatName) {
		switch (formatName.toLowerCase()) {
		case "wav":
			return VoiceFormat.wav;
		case "sp":
			return VoiceFormat.sp;
		case "silk":
			return VoiceFormat.silk;
		default:
			return VoiceFormat.wav;
		}
	}
}
