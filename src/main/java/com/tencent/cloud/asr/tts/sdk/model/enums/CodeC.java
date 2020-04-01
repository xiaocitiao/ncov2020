package com.tencent.cloud.asr.tts.sdk.model.enums;

/**
 * 合成方式： Pcm 或者 Opus
 * 
 * @author iantang
 * @version 1.0
 */
public enum CodeC {

	PCM("pcm"), OPUS("opus");

	private String code;

	private CodeC(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
