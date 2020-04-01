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

import static com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig.CUT_LENGTH;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.tencent.cloud.asr.realtime.sdk.http.base.HttpRequester;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBaseRequest;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;
import com.tencent.cloud.asr.realtime.sdk.utils.ByteUtils;

/**
 * 数据输入流请求
 * 
 * @author iantang
 * @version 1.0
 */
public class BaseRequestSender {

	private HttpRequester httpRequester = new HttpRequester();

	public VoiceResponse send(RasrBaseRequest rasrBasesRequest, InputStream inputStream) {
		VoiceResponse voiceResponse = null;
		/*long starttime = System.currentTimeMillis();*/
		try {
			int avaiable = inputStream.available();
			// System.out.print("data len is: " + avaiable); // Temp Test
			byte[] dataPacket = new byte[CUT_LENGTH];
			CloseableHttpClient httpclient = HttpClients.createDefault();
			int seq = 0, end = 0, readLength = 0;
			while (avaiable > 0) {
				if (dataPacket.length < CUT_LENGTH) // 通常不会出现
					dataPacket = new byte[CUT_LENGTH];
				readLength = inputStream.read(dataPacket);
				avaiable = inputStream.available(); // 剩下的字节数
				if (avaiable <= 0) {
					end = 1; // 读取到了最后一个分片
				}
				if (readLength < CUT_LENGTH) { // 通常只在最后一个分片出现
					dataPacket = ByteUtils.subBytes(dataPacket, 0, readLength);
				}
				/*System.out.println("readLength :" + readLength);*/
				rasrBasesRequest.updateSeqAndEndFlag(seq, end);
				voiceResponse = this.httpRequester.sendAndParse(rasrBasesRequest, dataPacket);
				seq++;
			}
			httpclient.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				// ignore
			}
		}
		/*long endtime = System.currentTimeMillis();
		System.out.println("程序运行时间为：" + (endtime - starttime) / 1000 + "秒");*/
		return voiceResponse;
	}
}
