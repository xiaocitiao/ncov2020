package com.tencent.cloud.asr.tts.sdk.asyn_sender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.tencent.cloud.asr.realtime.sdk.asyn_sender.ReceiverEntrance;
import com.tencent.cloud.asr.realtime.sdk.cache_handler.ReceiverCache;
import com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.tts.sdk.config.TtsConfig;
import com.tencent.cloud.asr.tts.sdk.http.base.TtsPcmRequester;
import com.tencent.cloud.asr.tts.sdk.utils.LineSplitUtils;

/**
 * TTS SDK程序请求入口，异步方法接收数据，并提供回复获取方法。 以实时语音SDK为基础，封装后形成适合TTS SDK的形态。
 * 
 * <p>
 * <b>线程安全</b> 做了基本处理，初步来看线程安全。
 * 
 * @author iantang
 * @version 1.0
 */
public class TtsEntrance extends ReceiverEntrance {

	private TtsReceiverCache ttsReceiverCache;

	// 因为重用了Rasr SDK的代码，因为用下面的变量来声明TTS SDk需要连接到的URL
	static {
		AsrInternalConfig.REAL_ASR_URL = "https://tts.cloud.tencent.com/stream";
		AsrInternalConfig.SIGN_URL = "https://tts.cloud.tencent.com/stream";
		AsrGlobelConfig.CUT_LENGTH = 819200;// 每次发往服务端的文字字节长度，建议设置一个较大的值，比如800KB。
		AsrInternalConfig.RECEIVER_CACHE_QUEUE_MAX_SIZE = 1000000; // 设置100万行缓存，足够用户快速add所有的文本，add越快，内存占用越多。
	}

	public TtsEntrance() {
		super();
		this.resetHttpRequester();
	}

	public TtsEntrance(long serviceId) {
		super(serviceId);
		this.resetHttpRequester();
	}

	private void resetHttpRequester() {
		super.getRequestService().getAsynRequestSender().setHttpRequester(new TtsPcmRequester());
	}

	/**
	 * 添加一句文字，文字的编码与配置的编码一致。
	 */
	public void add(String text) {
		this.add(text, UUID.randomUUID().toString(), TtsConfig.REQUEST_ENCODE.getCharset());
	}

	/**
	 * 添加一句文字，且指定sessionId
	 */
	public void add(String text, String sessionId) {
		this.add(text, sessionId, TtsConfig.REQUEST_ENCODE.getCharset());
	}

	/**
	 * 添加一句文字，且指定sessionId和文字的编码集。
	 */
	public void add(String text, String sessionId, Charset charset) {
		this.ttsReceiverCache.setSessionId(sessionId); // 间接为Requester对象赋值sessionId
		super.add(text.getBytes(charset), true);
	}

	/**
	 * 增加Tts数据到缓存且标识当前这份数据是一句话的结尾, 也确保肯定添加到了SDK的缓存中。
	 * 
	 * 如果当前缓存满了，会一直阻塞。
	 * 
	 * @return 通常会返回true。用户可忽略此返回值。
	 */
	public boolean addUntilSuccess(byte[] content) {
		return super.addUntilSuccess(content, true);
	}

	/**
	 * 增加Tts数据到缓存且标识当前这份数据是一句话的结尾, 也确保肯定添加到了SDK的缓存中。指定了sessionId。
	 * 
	 * 如果当前缓存满了，会一直阻塞。
	 * 
	 * @return 通常会返回true。用户可忽略此返回值。
	 */
	public boolean addUntilSuccess(byte[] content, String sessionId) {
		this.ttsReceiverCache.setSessionId(sessionId);
		return super.addUntilSuccess(content, true);
	}

	/**
	 * 标识传入的数据已到达一句话的结尾，普通用户不需要调研，请忽略本方法。因为其它的Add方法已默认为：每次传入的都是一句完整的话。
	 */
	@Override
	public void voiceEnd() {
		super.voiceEnd();
	}

	/**
	 * 添加一个文件，按行和标点切割文件中的文字。
	 */
	public void add(File file) {
		this.add(file, TtsConfig.REQUEST_ENCODE.getCharset(), UUID.randomUUID().toString());
	}

	/**
	 * 添加一个文件，按行和标点切割文件中的文字，且指定sessionId。
	 */
	public void add(File file, String sessionId) {
		this.add(file, TtsConfig.REQUEST_ENCODE.getCharset(), sessionId);
	}

	/**
	 * 添加一个文件，按行和标点切割文件中的文字，且指定sessionId和文字的编码集。
	 */
	public void add(File file, Charset charset, String sessionId) {
		if (file == null) {
			System.err.println("file can't be null.");
			return;
		}
		if (!file.exists()) {
			System.err.println("File not exist:" + file.getName() + ", skip add.");
			return;
		}
		BufferedReader in = null;
		try {
			int counter = 0;
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (StringUtils.isBlank(line))
					continue;
				List<String> itemList = LineSplitUtils.smartSplit(line); // 可以认为，已经切分成很合适的长度。
				for (String item : itemList) {
					this.add(item, sessionId + "-" + counter, charset);
					counter++;
				}
			}
		} catch (IOException e) {
			System.err.println("Read text file" + file.getAbsolutePath() + " failed, please check: " + e.getMessage());
		} finally {
			try {
				in.close();
			} catch (Exception e2) {
				// ignore.
			}
		}
	}

	/**
	 * 普通用户不需要调研，请忽略本方法。
	 */
	@Override
	public boolean add(byte[] content, boolean endFlag) {
		return super.add(content, endFlag);
	}

	/**
	 * 普通用户不需要调研，请忽略本方法。
	 */
	@Override
	public boolean addUntilSuccess(byte[] content, boolean endFlag) {
		return super.addUntilSuccess(content, endFlag);
	}

	@Override
	protected ReceiverCache createReceiverCache() {
		this.ttsReceiverCache = new TtsReceiverCache();
		return this.ttsReceiverCache;
	}
}
