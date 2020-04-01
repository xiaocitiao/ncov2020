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
package com.tencent.cloud.asr.realtime.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.cloud.asr.realtime.sdk.utils.JacksonUtil;

/**
 * 每次收到的回复字符串解析后获得的对象。实例和字段值解释如下：
 * 
 * <pre>
 * 回复实例：{"code":0,"message":"成功","voice_id":"EZ8BNIzby74hPX87","seq":6,"text":"吃饭了吗。"}
 * 
 * 字段解释：
 * code		正常时为0; 不正常时为其它值
 * message	值为：success，或者其它：错误的原因信息
 * voice_id	表示这通音频的标记，同一个音频流这个标记一样
 * seq		语音分片的序列号。备用字段，与腾讯云API3.0的要求保持一致。
 * text		语音转换的结果。备用字段，与腾讯云API3.0的要求保持一致。
 * </pre>
 * 
 * @author iantang
 * @version 1.0
 */
public class VoiceResponse {

	private int code;

	private String message;

	@JsonProperty("voice_id")
	private String voiceId = "";

	private int seq;

	private String text;

	@JsonIgnore
	private String originalText;

	/** 保存各项延迟统计的结果 */
	@JsonIgnore
	private TimeStat timeStat = new TimeStat();

	/**
	 * 默认的构造函数。不能删，因为{@link JacksonUtil#parse(String, Class)}方法会用到。
	 */
	public VoiceResponse() {
		// do nothing
	}

	public VoiceResponse(int code, String originalText) {
		this.code = code;
		this.originalText = originalText;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getVoiceId() {
		return voiceId;
	}

	public void setVoiceId(String voiceId) {
		this.voiceId = voiceId;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@JsonIgnore
	public String getOriginalText() {
		return originalText;
	}

	@JsonIgnore
	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	@JsonIgnore
	public TimeStat getTimeStat() {
		return timeStat;
	}

	/**
	 * 是否为整段语音的结尾（注意不是VAD语音段落的结尾，需要调用ReceiverEntrance的voiceEnd()方法后，本变量才有机会为true）。
	 */
	@JsonIgnore
	public boolean isEndCut() {
		return this.timeStat.isEndCut();
	}

	/**
	 * 是否包含结束分片的回复。
	 * 
	 * 子类VadResponse的判定标准不同：如果包含中间断开句子的结尾，也返回true。
	 */
	public boolean containsEndResult() {
		return this.isEndCut();
	}

	/**
	 * 比较不同点，如果code、oiceId、或者Text有变化，就说明是新的回复。
	 * 
	 * 暂未被使用过。
	 */
	public boolean compareDiff(VoiceResponse previous) {
		if (previous == null)
			return true;
		if (previous.getCode() != this.code || !previous.getVoiceId().equals(this.voiceId))
			return true;
		if (this.originalText != null && !previous.getOriginalText().equals(this.originalText))
			return true;
		return false;
	}

}
