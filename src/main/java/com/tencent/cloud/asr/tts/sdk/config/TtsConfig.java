package com.tencent.cloud.asr.tts.sdk.config;

import com.tencent.cloud.asr.tts.sdk.model.enums.CodeC;
import com.tencent.cloud.asr.tts.sdk.model.enums.RequestEncode;

public class TtsConfig {

	/**
	 * PCM or OPUS
	 */
	public static CodeC CODEC = CodeC.PCM;

	/**
	 * 传入的数据所采用的编码。请务必设置正确，若配置错误，则合成的语音也会不正常。
	 */
	public static RequestEncode REQUEST_ENCODE = RequestEncode.UTF_8;

	/**
	 * 语速，范围：[-2，2]，分别对应不同语速： -2代表0.6倍 -1代表0.8倍 0代表1.0倍（默认） 1代表1.2倍 2代表1.5倍 输入除以上整数之外的其他参数不生效，按默认值处理。
	 */
	public static int SPEED = 0;

	/**
	 * 音色： 0：亲和女声（默认） 1：亲和男声 2：成熟男声 3：活力男声 4：温暖女声 5：情感女声 6：情感男声
	 */
	public static int VOICE_TYPE = 0;

	/**
	 * 音量大小，范围：[0，10]，分别对应11个等级的音量，默认为0，代表正常音量。没有静音选项。 输入除以上整数之外的其他参数不生效，按默认值处理。
	 */
	public static int VOLUME = 0;

	/**
	 * 音频采样率： 16000：16k（默认） 8000：8k
	 */
	public static int SAMPLE_RATE = 16000;

	/**
	 * 主语言类型： 1-中文（默认） 2-英文
	 */
	public static int PRIMARY_LANGUAGE = 1;

	/**
	 * Http请求失败后的重试次数。
	 * 
	 * 无论是连接超时，还是接收数据失败，都算一次
	 */
	public static int HTTP_RETRY_TIMES = 2;

	// ----------------------------- 下面的配置通常不需要修改 ------------------------------
	/**
	 * Http连接超时时间，单位：毫秒。
	 */
	public static int HTTP_CONNECT_TIME_OUT = 1000;

	/**
	 * Http数据读取超时时间，单位：毫秒。
	 */
	public static int HTTP_READ_TIME_OUT = 1000;

	/**
	 * 每次发出请求时，最多携带的字符数。如果一行文字的长度超过此值，则会被截成多条请求发出。
	 */
	public static int SEPARATOR_LENGTH_LIMIT = 100;

	/**
	 * 对每行文字做分割时的关键字，遇到这里的字符肯定会切分开。
	 */
	public static String[] SEPARATOR_CHARS = new String[] { "。", "！", "？", "!", "?", "." };

}
