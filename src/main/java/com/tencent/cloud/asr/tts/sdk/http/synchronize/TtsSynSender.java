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
package com.tencent.cloud.asr.tts.sdk.http.synchronize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.tts.sdk.config.TtsConfig;
import com.tencent.cloud.asr.tts.sdk.http.base.TtsPcmRequester;
import com.tencent.cloud.asr.tts.sdk.model.TtsBytesRequest;
import com.tencent.cloud.asr.tts.sdk.model.TtsResponse;
import com.tencent.cloud.asr.tts.sdk.utils.LineSplitUtils;

public class TtsSynSender {

	private TtsPcmRequester ttsPcmRequester = new TtsPcmRequester();

	// 因为重用了Rasr SDK的代码，因为用下面的变量来声明TTS SDk需要连接到的URL
	static {
		AsrInternalConfig.REAL_ASR_URL = "https://tts.cloud.tencent.com/stream";
		AsrInternalConfig.SIGN_URL = "https://tts.cloud.tencent.com/stream";
	}

	public TtsResponse request(String text) {
		return this.request(text, UUID.randomUUID().toString());
	}

	public TtsResponse request(String text, String sessionId) {
		TtsBytesRequest ttsBytesRequest = new TtsBytesRequest(text);
		ttsBytesRequest.setSessionId(sessionId);
		TtsResponse ttsResponse = (TtsResponse) ttsPcmRequester.sendAndParse(ttsBytesRequest,
				ttsBytesRequest.createRequestBody());
		return ttsResponse;
	}

	public List<TtsResponse> requestFromFile(String filePath) {
		return this.requestFromFile(filePath, TtsConfig.REQUEST_ENCODE.getCharset(), UUID.randomUUID().toString());
	}

	public List<TtsResponse> requestFromFile(String filePath, Charset charset, String sessionId) {
		List<TtsResponse> list = new ArrayList<TtsResponse>();
		File file = new File(filePath);
		if (!file.exists()) {
			System.err.println("File not exist: " + filePath);
			return list;
		}
		BufferedReader in = null;
		try {
			int counter = 0;
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset));
			String line = null;
			while ((line = in.readLine()) != null) {
				if (StringUtils.isBlank(line))
					continue;
				List<String> itemList = LineSplitUtils.smartSplit(line);
				for (String item : itemList) {
					TtsResponse ttsResponse = this.request(item, sessionId + "-" + counter);
					if (ttsResponse != null)
						list.add(ttsResponse);
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
		return list;
	}
}
