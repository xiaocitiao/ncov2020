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
package com.tencent.cloud.asr.realtime.sdk.http.base;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.tencent.cloud.asr.realtime.sdk.config.AsrBaseConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrPersonalConfig;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBaseRequest;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponseParser;
import com.tencent.cloud.asr.realtime.sdk.utils.SignBuilder;

/**
 * 基础的Http请求对象，所有的请求都是通过调用本对象的方法发出的。
 * 
 * @author iantang
 * @version 1.0
 */
public class HttpRequester {

	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private String logCache;
	private CloseableHttpClient httpClient;
	private RequestConfig requestConfig;
	private HttpClientContext context = HttpClientContext.create(); // 每个线程单独使用自己的context

	public HttpRequester() {
		if (AsrGlobelConfig.USE_CUSTOM_CONNECTION_POOL) {
			this.httpClient = HttpPoolingManager.getInstance().getHttpClient();
		} else {
			this.httpClient = HttpClients.createDefault();
			this.requestConfig = RequestConfig.custom().setConnectTimeout(500).setSocketTimeout(300000).build();
		}
	}

	/**
	 * 创建serverUrl，计算签名，发出请求，收到回复后parse成VoiceResponse对象返回。
	 */
	public VoiceResponse sendAndParse(RasrBaseRequest rasrBasesRequest, byte[] dataPacket) {
		// 发请求，收取字符串回复。
		String responseStr = (String)this.send(rasrBasesRequest, dataPacket);
		// 转成VoiceResponse对象：
		VoiceResponse voiceResponse = null;
		if (responseStr != null && responseStr.contains("code") && responseStr.contains("seq")) {
			voiceResponse = VoiceResponseParser.parse(responseStr); // 解析Json会消耗1~2ms时间，已测试。
		} else
			System.err.println("Unexpected http response: " + responseStr);
		return voiceResponse;
	}

	/**
	 * 计算签名，发出请求，返回收到的回复字符串。
	 */
	public Object send(RasrBaseRequest rasrBasesRequest, byte[] dataPacket) {
		// 1. 创建Url：
		rasrBasesRequest.generateUrl();
		// 2. 签名
		String sign = SignBuilder.createPostSign(rasrBasesRequest.getSignUrl(), rasrBasesRequest.getSecretKey());
		/*System.out.println("签名: " + sign);*/
		// 3. 请求
		return this.send(rasrBasesRequest.getServerUrl(), sign, dataPacket);
	}

	/**
	 * 发出请求到指定Url，返回收到的回复。
	 */
	public Object send(String serverUrl, String sign, byte[] dataPacket) {
		// 3、设置header
		HttpPost hpost = new HttpPost(serverUrl);
		if (!AsrGlobelConfig.USE_CUSTOM_CONNECTION_POOL) {
			hpost.setConfig(this.requestConfig);
		}
		hpost.setHeader("Authorization", sign);
		hpost.setHeader("Content-Type", "application/octet-stream");
		hpost.setEntity(new ByteArrayEntity(dataPacket));
		/*long begin = System.currentTimeMillis(); */
		CloseableHttpResponse response = null;
		String responseStr = null;
		this.printCutRequest(serverUrl);
		response = retryGetResponse(hpost);
		if (response == null)
			return null;
		try {
			responseStr = EntityUtils.toString(response.getEntity(), AsrPersonalConfig.responseEncode.getName());
			this.printCutResponse(responseStr);
			if (response.getStatusLine().getStatusCode() != 200) {
				System.err.println("Request failed, response statusLine: " + response.getStatusLine() + ", response: "
						+ responseStr);
				hpost.abort();
			}
		} catch (Exception e) {
			if (e.getMessage().indexOf("connect timed out") >= 0) {
				System.err.println("Connet failed: " + e.getMessage());
			} else {
				System.err.println("Failed get response: " + e.getMessage());
			}
			hpost.abort();
			return null;
		} finally {
			try {
				InputStream inputStream = response.getEntity().getContent();
				inputStream.close();
			} catch (Exception e) {
				// ignore
			}
			try {
				response.close();
			} catch (Exception e) {
				// ignore
			}
		}
		/*long finished = System.currentTimeMillis();
		System.err.println("Current timestamp: " + finished + ", cost: " + (finished - begin) + " ms. ");*/
		return responseStr;
	}

	/**
	 * 增加了重试机制，确保获取到正确的回复。
	 * 
	 * 连接超时则重试3次（一共尝试4次），回复内容异常时重新请求一次。
	 */
	private CloseableHttpResponse retryGetResponse(HttpPost hpost) {
		CloseableHttpResponse response = null;
		for (int i = 0; i <= AsrGlobelConfig.RECONNECTION_TIMES; i++) {
			if (i > 0)
				System.out.println("Retry create connection as timeout: " + i);
			response = excuteOnce(hpost);
			// 出现连接失败等原因，开始下一次尝试
			if (response == null)
				continue;

			// 返回的数据异常时，多请求一次。
			if (response.getStatusLine().getStatusCode() != 200) {
				try {
					response.getEntity().getContent().close();// 加上此句，重跑execute时才不会卡死
				} catch (Exception e) {
					continue;
				}
				System.out.println("Re running as status not 200: " + response.getStatusLine().getStatusCode());
				response = excuteOnce(hpost);
			}
			return response;
		}
		System.err.println("Failed get response as: " + this.logCache);
		return response;
	}

	private CloseableHttpResponse excuteOnce(HttpPost hpost) {
		try {
			return this.httpClient.execute(hpost, this.context);
		} catch (Exception e) {
			if (e.getMessage().indexOf("connect timed out") >= 0) {
				this.logCache = "Timeout Error: " + e.getMessage();
			} else {
				System.err.println("Http post request failed: " + e.getMessage());
			}
			// 简化处理：不管什么原因，先返回null
			return null;
		}
	}

	private void printCutResponse(String responseStr) {
		if (AsrBaseConfig.PRINT_CUT_RESPONSE)
			System.out.println(sdf.format(new Date()) + " Cut response: " + responseStr);
	}

	private void printCutRequest(String serverUrl) {
		if (AsrBaseConfig.PRINT_CUT_REQUEST)
			System.out.println(sdf.format(new Date()) + " Cut request: " + serverUrl);
	}

	public void closeClient() {
		try {
			this.httpClient.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
