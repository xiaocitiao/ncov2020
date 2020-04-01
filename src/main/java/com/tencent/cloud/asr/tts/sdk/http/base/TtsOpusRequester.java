package com.tencent.cloud.asr.tts.sdk.http.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.tencent.cloud.asr.realtime.sdk.http.base.HttpRequester;
import com.tencent.cloud.asr.realtime.sdk.utils.ByteUtils;
import com.tencent.cloud.asr.tts.sdk.config.TtsConfig;
import com.tencent.cloud.asr.tts.sdk.utils.BytesReader;

/**
 * 《注意》： 开发测试中，功能未完成。
 * 
 * 没找到Java版的opus的解码Jar，所以收完数据以后较难处理。
 * 
 * 原生的Http请求，改用了JDK自带的Http请求发送类。
 * 
 * <pre>
 * 因为TTS是有了Trunk协议发送数据流，需要做解码，担心如果HttpClient会过滤掉一些消息头，所以先行使用原生的Http请求类处理。
 * 后续有机会时可以考虑改成HttpClient做测试并实现功能。
 * </pre>
 * 
 * @author iantang
 * @version 1.0
 */
public class TtsOpusRequester extends HttpRequester {

	// @Override
	// public VoiceResponse sendAndParse(RasrBaseRequest rasrBasesRequest, byte[] dataPacket) {
	// return super.sendAndParse(rasrBasesRequest, dataPacket);
	// }
	//
	// @Override
	// public String send(RasrBaseRequest rasrBasesRequest, byte[] dataPacket) {
	// return super.send(rasrBasesRequest, dataPacket);
	// }

	@Override
	public String send(String serverUrl, String sign, byte[] dataPacket) {
		String response = null;
		for (int i = 0; i < TtsConfig.HTTP_RETRY_TIMES; i++) {
			if (i > 0)
				System.out.println("Retry times: " + i + ", send http request...");
			response = this.request(serverUrl, sign, dataPacket);
			if (response != null)
				return response;
		}
		System.err.println("Failed get response. please check network or contact us.");
		return null;
	}

	private String request(String serverUrl, String sign, byte[] dataPacket) {
		try {
			// http post请求
			URL url = new URL(serverUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Authorization", sign);
			conn.connect();
			OutputStream out = conn.getOutputStream();
			out.write(dataPacket);
			out.flush();
			out.close();
			InputStream inputStream = conn.getInputStream();
			this.readAndDecode(inputStream);
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		return null;
	}

	private String readAndDecode(InputStream inputStream) throws IOException {
		boolean res = false;
		byte[] responseDatas = new byte[0];
		while (!Thread.currentThread().isInterrupted()) {
			// read header
			byte[] headBytes = new byte[4];
			res = this.read(inputStream, headBytes);
			if (!res) {
				System.err.println("read header bytes failed.");
				return null;
			}
			if (!verifyHeader(headBytes)) {
				System.err.println("Get header values abnormal, not opus but: " + new String(headBytes));
				return null;
			}
			// read seq
			byte[] seqBytes = new byte[4];
			this.read(inputStream, seqBytes);
			int seq = BytesReader.toInt(seqBytes);
			if (seq < -1) {
				System.err.println("Get seq abnormal: " + seq);
				return null;
			} else if (seq == -1) { // 读完了。
				int left = inputStream.available();
				byte[] leftBytes = new byte[left];
				res = this.read(inputStream, leftBytes);
				System.out.println(new String(leftBytes)); // 临时打印
				return new String(responseDatas);
			}
			// read pkg size
			byte[] lengthBytes = new byte[4];
			this.read(inputStream, lengthBytes);
			int length = BytesReader.toInt(lengthBytes);
			if (length <= 0) {
				System.err.println("Get package length abnormal: " + length);
				return null;
			}
			// read pkg
			byte[] datas = new byte[length];
			res = this.read(inputStream, datas);
			if (!res) {
				System.err.println("Failed get opus data. ");
				return null;
			}
			// new OpusDecoder();
			// datas = decoder.decodeTTSData(seq, datas); //decode
			responseDatas = ByteUtils.concat(responseDatas, datas);
		}
		return new String(responseDatas);
	}

	private boolean verifyHeader(byte[] headBuffer) {
		String header = new String(headBuffer);
		return header.equals("opus");
	}

	private boolean read(InputStream in, byte[] buffer) throws IOException {
		int length = buffer.length;
		int offset = 0;
		while (true) {
			int count = length - offset;
			int currentRead = in.read(buffer, offset, count);
			if (currentRead >= 0) {
				offset += currentRead;
				if (offset == length) {
					return true;
				}
			}
			if (currentRead == -1) {
				return false;
			}
		}
	}

	/*// 下面的处理方法有问题，还没空处理。
	private byte[] decode(byte[] packetData) {
		int frameSize = Opus.decoder_get_nb_samples(opusState, packetData, 0, packetData.length);
		int decodedSamples = Opus.decode(opusState, packetData, 0, packetData.length, decodeBuffer.array(), 0,
				frameSize, 0);
		if (decodedSamples < 0) {
			System.out.println("Decode error: " + decodedSamples);
			decodeBuffer.clear();
			return null;
		}
		decodeBuffer.position(decodedSamples * 2); // 2 bytes per sample
		decodeBuffer.flip();

		byte[] decodedData = new byte[decodeBuffer.remaining()];
		decodeBuffer.get(decodedData);
		decodeBuffer.flip();
		System.out.println(String.format("Encoded frame size: %d bytes", packetData.length));
		System.out.println(String.format("Decoded frame size: %d bytes", decodedData.length));
		System.out.println(String.format("Decoded %d samples", decodedSamples));
		return decodedData;
	}*/
}
