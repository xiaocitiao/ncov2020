package com.tencent.cloud.asr.realtime.sdk.utils;

import java.util.List;
import java.util.Map;

import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.model.enums.SdkRole;
import com.tencent.cloud.asr.realtime.sdk.model.response.VadResponse;
import com.tencent.cloud.asr.realtime.sdk.model.response.VadResult;

/**
 * 功能测试，非单元测试。
 * 
 * @author iantang
 * @version 1.0
 */
public class JacksonUtilTest {

	/**
	 * 测试稍微复杂的Json元素组合,可以直接使用parse方法解析成功，只需将类对象与Json的格式对应好即可。
	 * 
	 * 以本SDK中的回复数据为例，下面的Json字符串，可以被解析成<code>VoiceResponse</code>对象。
	 * 
	 * <pre>
	 * {"code":1, "result_number":2, "result_list":
	 * [{"slice_type":1,"index":2,"start_time":4294923044,"end_time":4294923294,"voice_text_str":"你好，我来了"}
	 * , {"slice_type":2,"index":44,"start_time":4294923111,"end_time":4294923222,"voice_text_str":"第二句在此"}]}
	 * </pre>
	 * 
	 */
	public void testParse() {
		String str = "{\"code\":1, \"result_number\":2, \"result_list\":[{\"slice_type\":1,\"index\":2,\"start_time\":4294923044,\"end_time\":4294923294,\"voice_text_str\":\"你好，我来了\"}, {\"slice_type\":2,\"index\":44,\"start_time\":4294923111,\"end_time\":4294923222,\"voice_text_str\":\"第二句在此\"}]}";
		AsrInternalConfig.setSdkRole(SdkRole.VAD);
		VadResponse reponse = JacksonUtil.parse(str, VadResponse.class);
		System.err.println(reponse.getCode());
		System.err.println(reponse.getResultNumber());
		System.out.println(reponse.getResultList().get(1).getVoiceTextStr());
	}

	/**
	 * 测试List格式的Json字符串解析功能。结果正常。以下面Json字符串为例：
	 * 
	 * <pre>
	 * [{"slice_type":1,"index":2,"start_time":4294923044,"end_time":4294923294,"voice_text_str":"第一句"}
	 * , {"slice_type":2,"index":44,"start_time":4294923111,"end_time":4294923222,"voice_text_str":"第二句"}]
	 * </pre>
	 */
	public void testParseList() {
		String listStr = "[{\"slice_type\":1,\"index\":2,\"start_time\":4294923044,\"end_time\":4294923294,\"voice_text_str\":\"第一句\"}, {\"slice_type\":2,\"index\":44,\"start_time\":4294923111,\"end_time\":4294923222,\"voice_text_str\":\"第二句\"}]";
		List<VadResult> list = JacksonUtil.parseList(listStr.getBytes(), VadResult.class);
		for (VadResult vadResult : list) {
			System.err.println(vadResult.getVoiceTextStr());
		}
	}

	/**
	 * 测试Map格式的Json字符串解析功能。结果正常。以下面Json字符串为例：
	 * 
	 * <pre>
	 * {"result_1":{"slice_type":1,"index":2,"start_time":4294923044,"end_time":4294923294,"voice_text_str":"Map中的第一个value"}
	 * , "result_2":{"slice_type":2,"index":44,"start_time":4294923111,"end_time":4294923222,"voice_text_str":"Map中的第二个value"}}
	 * </pre>
	 */
	public void testParseMap() {
		String mapStr = "{\"result_1\":{\"slice_type\":1,\"index\":2,\"start_time\":4294923044,\"end_time\":4294923294,\"voice_text_str\":\"Map中的第一个value\"}, \"result_2\":{\"slice_type\":2,\"index\":44,\"start_time\":4294923111,\"end_time\":4294923222,\"voice_text_str\":\"Map中的第二个value\"}}";
		Map<String, VadResult> map = JacksonUtil.parseMap(mapStr, VadResult.class);
		for (String key : map.keySet()) {
			System.err.println("key: " + key + " --> " + map.get(key).getVoiceTextStr());
		}
	}

	public static void main(String[] args) {
		JacksonUtilTest jacksonUtilTest = new JacksonUtilTest();
		jacksonUtilTest.testParse();
		jacksonUtilTest.testParseList();
		jacksonUtilTest.testParseMap();
	}

}
