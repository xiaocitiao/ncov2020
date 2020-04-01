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
 * 引擎类型
 * 
 * @author iantang
 * @version 1.0
 */
public enum EngineModelType {

	_8k_0("8k_0"), //
	_16k_0("16k_0"), //
	_8k_6("8k_6"); // 仅用于离线识别

	private String name;

	private EngineModelType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static EngineModelType parse(String typeName) {
		if (typeName == null)
			throw new IllegalArgumentException("EngineModelType should not be null");
		switch (typeName.toLowerCase()) {
		case "8":
		case "8k":
		case "8k_0":
		case "_8k_0":
			return EngineModelType._8k_0;
		case "16":
		case "16k":
		case "16k_0":
		case "_16k_0":
			return EngineModelType._16k_0;
		case "8k_6":
		case "_8k_6":
			return EngineModelType._8k_6;
		default:
			System.err.println("Unrecognized EngineModelType: " + typeName + ", set with default: 8k");
			return EngineModelType._8k_0;
		}
	}
}
