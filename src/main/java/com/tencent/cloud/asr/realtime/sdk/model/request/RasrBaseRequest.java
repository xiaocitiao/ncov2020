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
package com.tencent.cloud.asr.realtime.sdk.model.request;

import static com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig.REAL_ASR_URL;
import static com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig.SIGN_URL;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.tencent.cloud.asr.realtime.sdk.config.AsrBaseConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrPersonalConfig;
import com.tencent.cloud.asr.realtime.sdk.model.enums.EngineModelType;
import com.tencent.cloud.asr.realtime.sdk.model.enums.ResponseEncode;
import com.tencent.cloud.asr.realtime.sdk.model.enums.ReturnType;
import com.tencent.cloud.asr.realtime.sdk.model.enums.VoiceFormat;
import com.tencent.cloud.asr.realtime.sdk.utils.StringTools;

/**
 * 本对象与常规对象的属性创建和使用方法稍有不同。添加了一些冗余数据。主要是为了小小提升项目的执行效率。
 * 
 * @author iantang
 * @version 1.0
 */
public class RasrBaseRequest {

	private String appId = AsrBaseConfig.appId;
	private String secretId = AsrBaseConfig.secretId;
	private String secretKey = AsrBaseConfig.secretKey;
	private int projectId = AsrInternalConfig.PROJECT_ID;
	private EngineModelType engineModelTtype = AsrPersonalConfig.engineModelType;
	private ResponseEncode responseEncode = AsrPersonalConfig.responseEncode;
	private VoiceFormat voiceFormat = AsrPersonalConfig.voiceFormat;
	private ReturnType returnType = AsrInternalConfig.returnType;
	private int subServiceType = AsrInternalConfig.SUB_SERVICE_TYPE;
	private String voiceId = StringTools.getRandomString(16); // 设为随机值
	private long timestamp = System.currentTimeMillis() / 1000; // 秒
	private long expired = this.timestamp + 86400; // 1天后过期
	private int nonce = createNonce();
	/*private String templateName;*/// 暂无模版功能。

	private int seq;
	private int endFlag;

	private String serverUrl;
	private String signUrl;

	private int needVad = AsrGlobelConfig.NEED_VAD; // 仅用于Vad版的SDK请求。

	public RasrBaseRequest() {
		// do nothing;
	}

	public RasrBaseRequest(RasrBaseRequest previous) {
		this.voiceId = previous.getVoiceId();
		this.timestamp = previous.getTimestamp();
		this.expired = previous.getExpired();
		this.nonce = previous.getNonce();
	}

	public void updateSeqAndEndFlag(int seq, int endFlag) {
		this.seq = seq;
		this.endFlag = endFlag;
	}

	/**
	 * 构建Http请求用的URL字符串和创建签名用的URL字符串。
	 * 
	 * <pre>
	 * 构建结果有效实例：
	 * VAD版：
	 * http://aairealtime.qcloud.com/asr/v1/1255628450?&end=0&engine_model_type=8k_0&expired=1555991966&needvad=1&nonce=2914&projectid=1013976&res_type=0&result_text_format=0&secretid=AKID31NbfXbpBLJ4kGJrytc9UfgVAlGltJJ8&seq=0&source=0&sub_service_type=1&timeout=200&timestamp=1555905566&voice_format=1&voice_id=b0dP0vIP1im8lLb9
	 * 在线版：
	 * http://aai.qcloud.com/asr/v1/1255628450?end=1&engine_model_type=8k_0&expired=1552552044&nonce=5925&projectid=1013976&res_type=0&result_text_format=0&secretid=AKID31NbfXbpBLJ4kGJrytc9UfgVAlGltJJ8&seq=7&source=0&sub_service_type=1&timeout=200&timestamp=1552465644&voice_format=1&voice_id=3QqYoj3ItnrfgUOA&
	 * </pre>
	 */
	public void generateUrl() {
		this.signUrl = this.createSignUrl();
		this.serverUrl = this.createServerUrl();
		/*System.out.println("SignUrl: " + signUrl);
		System.out.println("serverUrl: " + serverUrl);*/
	}

	/**
	 * 创建ServerUrl，对于实时SDK而言，它与签名url基本一样。仅仅是开头的部分可能会不同。
	 * 
	 * 其它比如离线的SDK，可覆盖本方法，实现自己的逻辑。
	 */
	protected String createServerUrl() {
		if (this.signUrl == null)
			this.signUrl = this.createSignUrl();
		String serverUrl = this.signUrl;
		if (!REAL_ASR_URL.equals(SIGN_URL))
			serverUrl = this.signUrl.replaceFirst(SIGN_URL, REAL_ASR_URL);
		return serverUrl;
	}

	/**
	 * 创建签名用的Url，实时Sdk中，此值与ServerUrl一致，仅仅是开头可能有些不同。
	 * 
	 * 其它比如离线识别、TTS的SDK，可以覆盖本方法，实现自己的逻辑。
	 */
	protected String createSignUrl() {
		StringBuilder sb = new StringBuilder(AsrInternalConfig.SIGN_URL);
		sb.append(this.appId).append("?");
		// to make that all the parameters are sorted by ASC order
		this.appendUriEntries(sb);
		/*System.out.println("Generated sign URL: " + sb.toString());*/
		return sb.toString();
	}

	protected void appendUriEntries(StringBuilder sb) {
		TreeMap<String, Object> treeMap = this.initTreeMap(); // 用的时候才创建
		TreeMap<String, Object> sortedMap = new TreeMap<String, Object>(treeMap);
		for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(entry.getValue());
			sb.append('&');
		}
		if (treeMap.size() > 0) {
			sb.setLength(sb.length() - 1); // 去掉最后面的 '&'号
		}
	}

	/**
	 * 初始化资料树，在对象创建时初始化，微弱提升后面的线程的处理速度。
	 */
	protected TreeMap<String, Object> initTreeMap() {
		TreeMap<String, Object> treeMap = new TreeMap<String, Object>();
		treeMap.put("secretid", this.secretId);
		treeMap.put("projectid", "" + this.projectId);
		treeMap.put("sub_service_type", "" + this.subServiceType);
		treeMap.put("engine_model_type", this.engineModelTtype.getName());
		treeMap.put("res_type", "" + this.returnType.getTypeId());
		treeMap.put("result_text_format", "" + this.responseEncode.getId());
		treeMap.put("voice_id", this.voiceId);
		treeMap.put("timeout", "200"); // 暂时默认为200ms
		treeMap.put("source", "0"); // 目前默认为0
		treeMap.put("voice_format", "" + this.voiceFormat.getFormatId());
		treeMap.put("timestamp", "" + this.timestamp);
		treeMap.put("expired", "" + this.expired); // 1天后过期
		treeMap.put("nonce", "" + this.nonce);
		treeMap.put("seq", "" + this.seq);
		treeMap.put("end", "" + this.endFlag);
		if (AsrInternalConfig.isVadRole())
			treeMap.put("needvad", "" + this.needVad);
		return treeMap;
	}

	public String getAppId() {
		return appId;
	}

	public String getSecretId() {
		return secretId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public int getProjectId() {
		return projectId;
	}

	public int getSubServiceType() {
		return subServiceType;
	}

	public EngineModelType getEngineModelTtype() {
		return engineModelTtype;
	}

	public ResponseEncode getResponseEncode() {
		return responseEncode;
	}

	public VoiceFormat getVoiceFormat() {
		return voiceFormat;
	}

	public ReturnType getReturnType() {
		return returnType;
	}

	public void setEngineModelTtype(EngineModelType engineModelTtype) {
		this.engineModelTtype = engineModelTtype;
	}

	public void setResponseEncode(ResponseEncode responseEncode) {
		this.responseEncode = responseEncode;
	}

	public void setVoiceFormat(VoiceFormat voiceFormat) {
		this.voiceFormat = voiceFormat;
	}

	public void setReturnType(ReturnType returnType) {
		this.returnType = returnType;
	}

	public String getVoiceId() {
		return voiceId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public long getExpired() {
		return expired;
	}

	public int getNonce() {
		return nonce;
	}

	public int getSeq() {
		return seq;
	}

	public int getEndFlag() {
		return endFlag;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getSignUrl() {
		return signUrl;
	}

	/**
	 * 手工初始化。目前暂未用到。
	 */
	public void manualInit(EngineModelType engineModelType, ResponseEncode responseEncode, VoiceFormat voiceFormat,
			ReturnType returnType) {
		this.engineModelTtype = engineModelType;
		this.responseEncode = responseEncode;
		this.voiceFormat = voiceFormat;
		this.returnType = returnType;
	}

	/**
	 * 获取1个1000--9999之间的随机数。
	 */
	private int createNonce() {
		return new Random().nextInt(9000) + 1000;
	}
}
