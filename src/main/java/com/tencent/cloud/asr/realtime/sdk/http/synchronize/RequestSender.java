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
package com.tencent.cloud.asr.realtime.sdk.http.synchronize;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBaseRequest;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBytesRequest;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrFileRequest;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;

public class RequestSender {
	
	private BaseRequestSender baseRequestSender = new BaseRequestSender();

	/**
	 * 从文件读取语音数据，发送请求。
	 */
	public VoiceResponse sendFromFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists())
			return new VoiceResponse(102, "File not exist: " + filePath);
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {// should not happen
			e.printStackTrace();
			return new VoiceResponse(102, "create FileInputStream failed: " + filePath);
		}
		RasrBaseRequest rasrBaseRequest = new RasrFileRequest(file);
		return this.baseRequestSender.send(rasrBaseRequest, inputStream);
	}

	/**
	 * 从字节数组读取语音数据，发送请求。
	 */
	public VoiceResponse sendFromBytes(byte[] content) {
		InputStream inputStream = new ByteArrayInputStream(content);
		RasrBaseRequest rasrBaseRequest = new RasrBytesRequest(content);
		return this.baseRequestSender.send(rasrBaseRequest, inputStream);
	}
}
