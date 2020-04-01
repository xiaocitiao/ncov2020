package com.tencent.cloud.asr.tts.sdk.http.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.tencent.cloud.asr.realtime.sdk.http.base.HttpRequester;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBaseRequest;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;
import com.tencent.cloud.asr.realtime.sdk.utils.ByteUtils;
import com.tencent.cloud.asr.tts.sdk.config.TtsConfig;
import com.tencent.cloud.asr.tts.sdk.model.TtsBytesRequest;
import com.tencent.cloud.asr.tts.sdk.model.TtsResponse;

/**
 * 原生的Http请求，改用了JDK自带的Http请求发送类。
 * 
 * <pre>
 * 因为TTS是有了Trunk协议发送数据流，需要做解码，担心如果HttpClient会过滤掉一些消息头，所以先行使用原生的Http请求类处理。
 * 后续有机会时可以考虑改成HttpClient做测试并实现功能。
 * </pre>
 * 
 * @author iantang
 * @version 1.0
 */
public class TtsPcmRequester extends HttpRequester {

	@Override
	public VoiceResponse sendAndParse(RasrBaseRequest rasrBasesRequest, byte[] dataPacket) {
		TtsBytesRequest ttsBytesRequest = (TtsBytesRequest) rasrBasesRequest;
		// 发请求，收取字符串回复。
		byte[] responseBytes = (byte[]) this.send(rasrBasesRequest, ttsBytesRequest.createRequestBody());
		if (responseBytes == null)
			return null;
		// 转成TtsResponse对象：
		return new TtsResponse(responseBytes, ttsBytesRequest.getSessionId());
	}

	@Override
	public byte[] send(String serverUrl, String sign, byte[] dataPacket) {
		byte[] response = null;
		for (int i = 0; i < TtsConfig.HTTP_RETRY_TIMES; i++) {
			if (i > 0)
				System.out.println("Retry times: " + i + ", send http request...");
			response = this.request(serverUrl, sign, dataPacket);
			if (response != null)
				return response;
		}
		System.err.println("Failed get response. please check network or contact us.");
		return null;
	}

	private byte[] request(String serverUrl, String sign, byte[] dataPacket) {
		try {
			// 发送POST请求
			URL url = new URL(serverUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", sign);
			conn.setConnectTimeout(TtsConfig.HTTP_CONNECT_TIME_OUT);
			conn.setReadTimeout(TtsConfig.HTTP_READ_TIME_OUT);
			conn.connect();

			OutputStream out = conn.getOutputStream();
			/*System.out.println("Request Body: " + new String(dataPacket, TtsConfig.REQUEST_ENCODE.getCharset()));*/
			out.write(dataPacket);
			out.flush();
			out.close();
			InputStream inputStream = conn.getInputStream();
			return this.read(inputStream);
		} catch (Exception e) {
			System.err.println("Send Request failed: " + e.getMessage());
			e.printStackTrace(); // TODO临时测试
		}
		return null;
	}

	private byte[] read(InputStream inputStream) throws IOException {
		// long start = System.currentTimeMillis();
		byte[] responseDatas = new byte[0];
		while (true) {
			byte[] pcmData = new byte[10240]; // 经实测inputStream.available()并不能加速接收，因此使用固定值。
			try {
				int readSize = fill(inputStream, pcmData);
				/* System.out.println("read size: " + readSize); */
				if (readSize == 0) { // 加此判断相当于多尝试了一次读数据，确保数据都收完了。
					break;
				}
				if (readSize < pcmData.length) {
					pcmData = ByteUtils.subBytes(pcmData, 0, readSize);
				}
				responseDatas = ByteUtils.concat(responseDatas, pcmData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// long end = System.currentTimeMillis();
		// System.out.println("Size: " + responseDatas.length + ", cost: " + (end - start) + " ms.");
		return responseDatas;
	}

	private static int fill(InputStream in, byte[] buffer) throws IOException {
		int length = buffer.length;
		int offset = 0;
		while (true) {
			int count = length - offset;
			int currentRead = in.read(buffer, offset, count);
			if (currentRead >= 0) {
				offset += currentRead;
				/* System.out.println("offset: "+offset); */
				if (offset == length) {
					return length;
				}
			}
			if (currentRead == -1) { // 表示数据已收完
				return offset;
			}
		}
	}
}
