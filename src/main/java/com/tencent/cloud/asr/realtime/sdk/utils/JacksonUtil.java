package com.tencent.cloud.asr.realtime.sdk.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Json字符串和Json对象的相互转换工具。
 * 
 * 参考过资料：https://blog.csdn.net/zhuyijian135757/article/details/38269715， Author：zhuyijian135757
 * 
 * @author iantang
 * @version 1.0
 */
public class JacksonUtil {

	public JacksonUtil() {
		// do nothing
	}

	/**
	 * 将一个Json格式的字符串解析成指定的类对象。可以解析相对复杂的格式组合，只需将类对象与Json的格式对应好即可。
	 */
	public static <T> T parse(String content, Class<T> clz) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		try {
			return objectMapper.readValue(content, clz);
		} catch (IOException e) {
			System.err.println("Failed parse Json for content: " + content + ", error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * 解析：外层以List方式组织的Jason格式字符串，将字符串解析成指定的类对象List。
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> parseList(byte[] content, Class<T> clz) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, new Class[] { clz });
		try {
			return (List<T>) objectMapper.readValue(content, javaType);
		} catch (IOException e) {
			System.err.println("Failed parse Json List for content: " + content + ", error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * 解析：外层以Map方式组织的Jason格式字符串，将字符串解析成指定的类对象Map。
	 */
	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> parseMap(String jsonString, Class<T> clz) {
		ObjectMapper mapper = new ObjectMapper();
		JavaType javaType = mapper.getTypeFactory().constructParametricType(HashMap.class, String.class, clz);
		try {
			return (Map<String, T>) mapper.readValue(jsonString, javaType);
		} catch (IOException e) {
			System.err.println("Failed parse Json Map for String: " + jsonString + ", error: " + e.getMessage());
			return null;
		}
	}

	/**
	 * 将一个对象转换成Json字符串，传入的对象内容，也可以是一个HashMap，LinkedHashMap或者TreeMap等。
	 * 
	 * <pre>
	 * 特别介绍下传入Map的情况：
	 * 1. 传入HashMap时，返回的json字符串中的各个属性是无序的。
	 * 2. 传入LinkedHashMap时，返回的json字符串中的各个属性顺序与添加的属性相同。
	 * 3. 传入TreeMap时，返回的json字符串中的各个属性按照key中的字符的编码顺序来排序。
	 * </pre>
	 */
	public static String toJsonString(Object obj) {
		return toJsonString(obj, Include.NON_EMPTY);
	}

	public static String toJsonString(Object obj, Include include) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(include);

		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed serialize Json: " + e.getMessage(), e);
		}
	}

	/*public static void main(String[] args) {
		String response = "{\"code\":0,\"message\":\"成功\",\"voice_id\":\"iopd1AEanCd6Ogn9\",\"seq\":0,\"text\":\"\"}";
		VoiceResponse voiceResponse = parse(response, VoiceResponse.class);
		System.err.println(voiceResponse.getVoiceId());
	}*/
}
