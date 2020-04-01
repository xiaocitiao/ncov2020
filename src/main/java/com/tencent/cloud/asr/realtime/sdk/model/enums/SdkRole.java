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
 * 标识当前SDK扮演的角色，属于关键配置。
 * 
 * @author iantang
 * @version 1.0
 */
public enum SdkRole {

	ONLINE(1), VAD(2);

	private int roleId;

	private SdkRole(int roleId) {
		this.roleId = roleId;
	}

	public int getRoleId() {
		return roleId;
	}
}
