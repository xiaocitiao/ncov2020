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
package com.tencent.cloud.asr.realtime.sdk;

import com.tencent.cloud.asr.realtime.sdk.config.AsrBaseConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrPersonalConfig;
import com.tencent.cloud.asr.realtime.sdk.http.synchronize.RequestSender;
import com.tencent.cloud.asr.realtime.sdk.model.enums.EngineModelType;
import com.tencent.cloud.asr.realtime.sdk.model.enums.ResponseEncode;
import com.tencent.cloud.asr.realtime.sdk.model.enums.SdkRole;
import com.tencent.cloud.asr.realtime.sdk.model.enums.VoiceFormat;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;
import com.tencent.cloud.asr.realtime.sdk.utils.ByteUtils;

//import com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig;

/**
 * 同步调用实例。发一个请求，收到一个回复。回复收到之前，会一直阻塞。
 * 
 * @author iantang
 * @version 1.0
 */
public class RasrRequestSample {

	static {
		initBaseParameters();
	}

	public static void main(String[] args) {
		RasrRequestSample rasrRequestSample = new RasrRequestSample();
		rasrRequestSample.start();
	}

	private void start() {
		this.sendBytesRequest();
		// this.sleepSomeTime();
		// this.sendFileRequest();
		// this.sleepSomeTime();
		System.exit(0);
	}

	/**
	 * 从字节数组读取语音数据，发送请求。
	 */
	private void sendBytesRequest() {
		RequestSender requestSender = new RequestSender();
		byte[] content = ByteUtils.inputStream2ByteArray("test_wav/8k/8k.wav");
		VoiceResponse voiceResponse = requestSender.sendFromBytes(content);
		printReponse(voiceResponse);
	}

	/**
	 * 从文件读取语音数据，发送请求。
	 */
	// private void sendFileRequest() {
	// RequestSender requestSender = new RequestSender();
	// VoiceResponse voiceResponse = requestSender.sendFromFile("test_wav/8k.wav");
	// printReponse(voiceResponse);
	// }

	private void printReponse(VoiceResponse voiceResponse) {
		if (voiceResponse != null)
			System.out.println("Result is: " + voiceResponse.getOriginalText());
		else
			System.out.println("Result is null.");
	}

	/**
	 * 初始化基础参数, 请将下面的参数值配置成你自己的值。
	 * 
	 * 参数获取方法可参考： <a href="https://cloud.tencent.com/document/product/441/6203">签名鉴权 获取签名所需信息</a>
	 */
	private static void initBaseParameters() {
		// required
		// AsrBaseConfig.appId = "YOUR_APP_ID_SET_HERE";
		// AsrBaseConfig.secretId = "YOUR_SECRET_ID";
		// AsrBaseConfig.secretKey = "YOUR_SECRET_KEY";

		// optional，根据自身需求配置值
		AsrInternalConfig.setSdkRole(SdkRole.ONLINE); // VAD版用户请务必赋值为 SdkRole.VAD
		AsrPersonalConfig.responseEncode = ResponseEncode.UTF_8;
		AsrPersonalConfig.engineModelType = EngineModelType._8k_0;
		AsrPersonalConfig.voiceFormat = VoiceFormat.wav;

		// optional 可忽略
		// AsrGlobelConfig.NEED_VAD = 1; // 是否要做VAD，默认为1，表示要做。当前的线上版本SDK不适用，请忽略。
		// AsrGlobelConfig.CUT_LENGTH = 8192; //每次发往服务端的语音分片的大小设置，默认为8KB
		// AsrGlobelConfig.NOTIFY_ALL_RESPONSE = true; // 是否回调每个分片的回复，包括Vad后开始和中间的。如只需最后到的结果，可设为false。
		AsrBaseConfig.PRINT_CUT_REQUEST = true; // 是否打印中间的每个分片请求到控制台
		AsrBaseConfig.PRINT_CUT_RESPONSE = true; // 是否打印中间的每个分片请求的结果到控制台
		// 默认使用自定义连接池，连接数可在AsrGlobelConfig中修改，更多细节参数，可直接修改源码HttpPoolingManager.java,然后自行打Jar包。
		// AsrGlobelConfig.USE_CUSTOM_CONNECTION_POOL = true;
	}

	/*private void sleepSomeTime() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// ignore
		}
	}*/
}
