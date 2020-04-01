package com.tencent.cloud.asr.realtime.sdk.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vad断句后，某句话的识别结果。通常作为<code>VoiceResponse</code>中的变量之一，与之搭配使用。仅用于：拥有VAD功能的服务器 的回复对象的创建。
 * 
 * @author iantang
 * @version 1.0
 */
public class VadResult {

	@JsonProperty("slice_type")
	private int sliceType;

	private int index;

	@JsonProperty("start_time")
	private long startTime;

	@JsonProperty("end_time")
	private long endTime;

	@JsonProperty("voice_text_str")
	private String voiceTextStr;

	public VadResult() {
		// do nothing
	}

	/**
	 * 判断当前Vad断开的这句话的分析结果是否已完成（是否已分析完 全部分片）
	 */
	public boolean arrivedEnd() {
		return this.sliceType == 2;
	}

	public int getSliceType() {
		return sliceType;
	}

	public void setSliceType(int sliceType) {
		this.sliceType = sliceType;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getVoiceTextStr() {
		return voiceTextStr;
	}

	public void setVoiceTextStr(String voiceTextStr) {
		this.voiceTextStr = voiceTextStr;
	}
}