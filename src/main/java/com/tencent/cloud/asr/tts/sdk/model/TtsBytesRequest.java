package com.tencent.cloud.asr.tts.sdk.model;

import java.util.TreeMap;

import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBytesRequest;
import com.tencent.cloud.asr.realtime.sdk.utils.JacksonUtil;
import com.tencent.cloud.asr.tts.sdk.config.TtsConfig;
import com.tencent.cloud.asr.tts.sdk.model.enums.CodeC;

/**
 * TTS请求对象，要请求的文字保存在父类的字节数组列表中。每次请求都生成这样的一个对象，各对象之间的属性单独赋值，可自由修改，互不影响。
 * 
 * @author iantang
 * @version 1.0
 */
public class TtsBytesRequest extends RasrBytesRequest {

	/**
	 * PCM or OPUS
	 */
	private CodeC codec = TtsConfig.CODEC;

	/**
	 * 语速，范围：[-2，2]，分别对应不同语速： -2代表0.6倍 -1代表0.8倍 0代表1.0倍（默认） 1代表1.2倍 2代表1.5倍 输入除以上整数之外的其他参数不生效，按默认值处理。
	 */
	private int speed = TtsConfig.SPEED;

	/**
	 * 音色： 0：亲和女声（默认） 1：亲和男声 2：成熟男声 3：活力男声 4：温暖女声 5：情感女声 6：情感男声
	 */
	private int voiceType = TtsConfig.VOICE_TYPE;

	/**
	 * 音量大小，范围：[0，10]，分别对应11个等级的音量，默认为0，代表正常音量。没有静音选项。 输入除以上整数之外的其他参数不生效，按默认值处理。
	 */
	private int volume = TtsConfig.VOLUME;

	/**
	 * 音色. 0-亲和女声默认) 1-亲和男声 2-成熟男声 3-活力男声 4-温暖女声 5-情感女声 6-情感男声
	 */
	private int sampleRate = TtsConfig.SAMPLE_RATE;

	/**
	 * 主语言类型： 1-中文（默认） 2-英文
	 */
	private int primaryLanguage = TtsConfig.PRIMARY_LANGUAGE;

	/**
	 * 用户自定义的id号，可用于将回复和请求对应起来。
	 */
	private String sessionId = "session-123";

	public TtsBytesRequest() {
		super();
	}

	public TtsBytesRequest(RasrBytesRequest previous) {
		super(previous);
	}

	public TtsBytesRequest(byte[] contents) {
		super(contents);
	}

	/**
	 * @param text
	 *            要请求的文本
	 */
	public TtsBytesRequest(String text) {
		this(text, TtsConfig.CODEC);
	}

	/**
	 * @param text
	 *            要请求的文本
	 * @param codec
	 *            请求的类型：pcm 或者opus
	 */
	public TtsBytesRequest(String text, CodeC codec) {
		super.add(text.getBytes(TtsConfig.REQUEST_ENCODE.getCharset()));
		this.codec = codec;
		/* System.out.println(ttsBytesRequest.createRequestBody()); */
	}

	@Override
	public void add(byte[] content) {
		super.add(content);
		this.initTreeMap();
	}

	public byte[] createRequestBody() {
		TreeMap<String, Object> treeMap = this.initTreeMap();
		return JacksonUtil.toJsonString(treeMap).getBytes(TtsConfig.REQUEST_ENCODE.getCharset());
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	protected String createServerUrl() {
		return AsrInternalConfig.REAL_ASR_URL;
	}

	@Override
	protected String createSignUrl() {
		StringBuilder sb = new StringBuilder(AsrInternalConfig.SIGN_URL);
		sb.append("?");
		// to make that all the parameters are sorted by ASC order
		super.appendUriEntries(sb);
		/* System.out.println("Generated sign URL: " + sb.toString()); */
		return sb.toString();
	}

	/**
	 * 创建资料树，通常在对象创建时初始化，微弱提升后面的线程的处理速度。
	 */
	@Override
	protected TreeMap<String, Object> initTreeMap() {
		TreeMap<String, Object> treeMap = new TreeMap<String, Object>();
		treeMap.put("Action", "TextToStreamAudio");
		treeMap.put("AppId", Long.parseLong(super.getAppId()));
		treeMap.put("SecretId", super.getSecretId());
		treeMap.put("Timestamp", super.getTimestamp());
		treeMap.put("Expired", super.getExpired());
		treeMap.put("Text", this.createText());
		treeMap.put("SessionId", "session-123");
		treeMap.put("ModelType", 1); // 默认类型
		treeMap.put("Volume", this.volume);
		treeMap.put("Speed", this.speed);
		treeMap.put("ProjectId", 0); // 默认id
		treeMap.put("VoiceType", this.voiceType);
		treeMap.put("PrimaryLanguage", this.primaryLanguage);
		treeMap.put("SampleRate", this.sampleRate);
		treeMap.put("Codec", this.codec.getCode());
		return treeMap;
	}

	/*
	 * public static void main(String[] args) { TtsBytesRequest ttsBytesRequest =
	 * new TtsBytesRequest("你好呀，我看好你。");
	 * System.out.println(ttsBytesRequest.createRequestBody()); }
	 */

	private String createText() {
		if (super.getBytesList() == null)
			return "";
		String text = "";
		for (int i = 0; i < super.getBytesList().size(); i++) {
			text += new String(super.getBytesList().get(i), TtsConfig.REQUEST_ENCODE.getCharset());
		}
		return text;
	}
}
