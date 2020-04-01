package com.tencent.cloud.asr.realtime.sdk.model.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.cloud.asr.realtime.sdk.utils.JacksonUtil;

/**
 * 
 * 每次收到的Vad版语音识别回复字符串解析后获得的对象。实例和字段值解释如下：
 * 
 * <pre>
 * 回复实例(为方便浏览，使用两行来显示)：
 * {"code":0,"message":"success","voice_id":"ACi8lvCzwFqWvUjh","seq":0,"text":"","result_number":1,
 * "result_list":[{"slice_type":2,"index":1,"start_time":1215,"end_time":1230,"voice_text_str":"吃饭了吗。","final":0}]}
 * 
 * 每个字段的解释（来自罗老师的文档）：
 *  code
 * 	- 0 正常
 * 	- 其他 不正常
 * 
 * message
 * 	如果是0就是success，不是0就是错误的原因信息
 * 
 * voice_id
 * 	表示这通音频的标记，同一个音频流这个标记一样
 * 
 * seq
 *  语音分片的序列号。备用字段，与腾讯云API3.0的要求保持一致。
 *  
 * text
 *  语音转换的结果。备用字段，与腾讯云API3.0的要求保持一致。
 * 
 * result_number:
 * 	表示后面的result_list里面有几段结果，如果是0表示没有结果，可能是遇到中间是静音了。
 * 	如果是1表示result_list有一个结果， 在发给服务器分片很大的情况下可能会出现多个结果，
 * 	正常情况下都是1个结果。
 * 
 * result_list:
 * 	- slice_type: 返回分片类型标记， 0表示一小段话开始，1表示在小段话的进行中，2表示小段话的结束
 * 	- index 表示第几段话
 * 	- start_time  这个分片在整个音频流中的开始时间
 * 	- end_time 这个分片在整个音频流中的结束时间
 * 	- voice_text_str 识别结果
 * 
 * final:
 *   是否为本段语音的最后一条回复。0:不是。1：是。用户调用voiceEnd()方法后，此值才会等于1。
 * </pre>
 * 
 * @author iantang
 * @version 1.0
 */
public class VadResponse extends VoiceResponse {

	/**
	 * 默认的构造函数。不能删，因为{@link JacksonUtil#parse(String, Class)}方法会用到。
	 */
	public VadResponse() {
		// do nothing
	}

	@JsonProperty("result_number")
	private int resultNumber;

	@JsonProperty("result_list")
	private List<VadResult> resultList;

	@JsonProperty("final")
	private int finalFlag;

	/**
	 * 判断是否为结束分片的回复，或者本Response对象是否含有(被vad断开的)一句语音的完整识别结果，有1个及以上就返回true。
	 * 
	 * 按照文档，目前是根据sliceType的值来判断：0位开头分片。1位中间分片。2位结尾分片，即：一句语音解析完了。
	 */
	@Override
	public boolean containsEndResult() {
		if (super.isEndCut())
			return true;
		if (this.resultNumber == 0)
			return false;
		for (VadResult result : this.resultList) {
			if (result.arrivedEnd())
				return true;
		}
		return false;
	}

	public List<VadResult> getResultList() {
		return resultList;
	}

	public void setResultList(List<VadResult> resultList) {
		this.resultList = resultList;
	}

	public int getResultNumber() {
		return resultNumber;
	}

	public void setResultNumber(int resultNumber) {
		this.resultNumber = resultNumber;
	}

	public int getFinalFlag() {
		return finalFlag;
	}

	public void setFinalFlag(int finalFlag) {
		this.finalFlag = finalFlag;
	}
}
