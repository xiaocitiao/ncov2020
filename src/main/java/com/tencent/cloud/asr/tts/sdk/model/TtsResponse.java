package com.tencent.cloud.asr.tts.sdk.model;

import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;

public class TtsResponse extends VoiceResponse {

	/**
	 * 回复的Pcm或Opus数据 字节数组
	 */
	private byte[] responseBytes;

	/**
	 * 对应用户请求时赋值的sessionId
	 */
	private String sessionId;

	/**
	 * 默认的构造函数。
	 */
	public TtsResponse(byte[] responseBytes, String sessionId) {
		this.responseBytes = responseBytes;
		this.sessionId = sessionId;
	}

	public byte[] getResponseBytes() {
		return responseBytes;
	}

	public String getSessionId() {
		return sessionId;
	}

}
