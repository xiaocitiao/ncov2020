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
package com.tencent.cloud.asr.tts.sdk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.InputStreamReader;

import com.tencent.cloud.asr.realtime.sdk.cache_handler.FlowHandler;
import com.tencent.cloud.asr.realtime.sdk.model.response.TimeStat;
import com.tencent.cloud.asr.tts.sdk.asyn_sender.TtsEntrance;
import com.tencent.cloud.asr.tts.sdk.config.TtsConfig;
import com.tencent.cloud.asr.tts.sdk.model.TtsResponse;
import com.tencent.cloud.asr.tts.sdk.model.enums.RequestEncode;
//import com.tencent.cloud.asr.realtime.sdk.config.AsrBaseConfig;
//import com.tencent.cloud.asr.tts.sdk.model.enums.CodeC;

/**
 * 异步调用实例。使用步骤为：
 * 
 * <pre>
 *  1. 配置基本参数。只需执行一次。 
 *  2. 新建一个（或多个）服务 。
 *  3. 使用服务。包括：初始化服务 、注册回调Handler、发送数据（用户线程A） 、接收数据（用户线程B）
 *  4. 服务本身可以持续运行，不建议频繁创建和销毁服务。
 * </pre>
 * 
 * 详见本TtsTask类中的run()方法。
 * 
 * @author iantang
 * @version 1.0
 */
public class TtsAsynRequestSample {

	static {
		initBaseParameters();
	}

	/** 线程个数 */
	private int threadNumber = 2;
	/** 需要合成语音的文字所在的文件 */
	private String textFile = "testTtsFiles/test_article.txt";

	private List<TtsTask> taskList = new ArrayList<TtsTask>();

	public static void main(String[] args) {
		TtsAsynRequestSample ttsRequestSample = new TtsAsynRequestSample();
		ttsRequestSample.setArguments(args);
		ttsRequestSample.start();

		sleepSomeTime(600000); // 10分钟后停止示例程序。
		ttsRequestSample.stop();
		System.exit(0);
	}

	/**
	 * 根据需求启动多个任务，每个任务都独立运行，互不干扰。
	 */
	public void start() {
		for (int i = 1; i <= this.threadNumber; i++) {
			TtsTask ttsTask = new TtsTask(i, this.textFile);
			this.taskList.add(ttsTask);
			ttsTask.start();
			sleepSomeTime(20);
		}
	}

	/**
	 * 停止全部任务。
	 */
	public void stop() {
		for (TtsTask ttsTask : this.taskList) {
			ttsTask.stop();
		}
	}

	/**
	 * 初始化基础参数, 请将下面的参数值配置成你自己的值。配置可中途修改，正常情况下立即生效。
	 * 
	 * 参数获取方法可参考： <a href="https://cloud.tencent.com/document/product/441/6203">签名鉴权 获取签名所需信息</a>
	 */
	private static void initBaseParameters() {
		// Required
		// AsrBaseConfig.appId = "YOUR_APP_ID_SET_HERE";
		// AsrBaseConfig.secretId = "YOUR_SECRET_ID";
		// AsrBaseConfig.secretKey = "YOUR_SECRET_KEY";

		// optional，根据自身需求设置配置值， 不配则使用默认值。
		TtsConfig.VOLUME = 5; // 音量大小, 范围[0，10]，默认为0，表示正常音量。
		// TtsConfig.REQUEST_ENCODE = RequestEncode.UTF_8; // 传入的文字所采用的编码，默认为utf-8
		// TtsConfig.SPEED = 0; // 语速，范围[-2，2]. -2: 0.6倍; -1: 0.8倍; 0:1.0倍（默认）; 1: 1.2倍; 2: 1.5倍 。其他值：1.0 倍。
		// TtsConfig.VOICE_TYPE = 0; // 音色： 0：亲和女声（默认） 1：亲和男声 2：成熟男声 3：活力男声 4：温暖女声 5：情感女声 6：情感男声
		// TtsConfig.SAMPLE_RATE = 16000;// 音频采样率： 16000：16k（默认）; 8000：8k
		// TtsConfig.PRIMARY_LANGUAGE = 1;// 主语言类型： 1-中文（默认） 2-英文
		// TtsConfig.CODEC = CodeC.PCM; // 无需修改。暂未支持Opus方式。

		// optional,可忽略。直接添加文本文件时，自动切分长语句的参数配置
		// TtsConfig.SEPARATOR_LENGTH_LIMIT = 100;
		// TtsConfig.SEPARATOR_CHARS = new String[] { "。", "！", "？", "!", "?", "." };
	}

	private static void sleepSomeTime(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}

	/**
	 * 生成可执行Jar后，运行Jar时传入参数的简单处理。不建议这样子传参数。
	 * 
	 * 比如这个命令传入了3个参数： java -jar ttsSdk_run.jar 3 testTtsFiles/text_contents.txt utf-8
	 * 
	 * 详情可见：out_runnable_jar/command_reference.txt
	 */
	private void setArguments(String[] args) {
		if (args.length > 0)
			this.threadNumber = Integer.parseInt(args[0]); // 使用传入的参数赋值线程个数
		if (args.length > 1)
			this.textFile = args[1];
		if (args.length > 2)
			TtsConfig.REQUEST_ENCODE = RequestEncode.parse(args[2]);
	}
}

// --------------------------------------------------------------------------------------------------------------
/**
 * 单个任务对象示例。包含了服务线程的创建、启动和关闭方法。
 * 
 * @author iantang
 * @version 1.0
 */
class TtsTask {

	private TtsEntrance ttsEntrance;
	private int taskId;
	private String textFile;
	private TextAddingTask textAddingTask;

	public TtsTask(int taskId, String textFile) {
		this.taskId = taskId;
		this.textFile = textFile;
	}

	/**
	 * 创建和启动服务线程。包括：数据添加线程、发送线程、通知线程。
	 */
	public void start() {
		// 新建一个服务
		this.ttsEntrance = new TtsEntrance(taskId);
		// 启动服务
		this.ttsEntrance.start();
		// 注册N个回调Handler
		this.ttsEntrance.registerReponseHandler(new MyResponseHandler(this.taskId));
		// 开始添加数据
		this.textAddingTask = new TextAddingTask(this.ttsEntrance, textFile);
		this.textAddingTask.start();

		// 10秒后停止任务/关闭服务。如需一直使用，则不要调用它。
		/*this.sleepSomeTime(10000);
		this.stop();*/
	}

	public void stop() {
		this.textAddingTask.stop();
		this.ttsEntrance.stopService();
	}
}

// --------------------------------------------------------------------------------------------------------------
/**
 * 添加语音数据的任务类。模拟：独立线程，持续将语音片段（字节数据）添加到缓存。
 * 
 * @author iantang
 * @version 1.0
 */
class TextAddingTask {

	private TtsEntrance ttsEntrance;
	private String textFile;
	private boolean keepAdding = true;

	public TextAddingTask(TtsEntrance ttsEntrance, String textFile) {
		this.ttsEntrance = ttsEntrance;
		this.textFile = textFile;
	}

	public void start() {
		Thread thread = new Thread("message sender thread") {
			public void run() {
				repeatAddBytesRequest();
			};
		};
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * 停止添加线程（用户线程A）
	 */
	public void stop() {
		this.keepAdding = false;
	}

	/**
	 * 用户线程A：持续将语音片段（字节数据）添加到缓存。具体的发送和通知动作由其它线程负责。
	 * 
	 * 本方法仅作示例用途，用户可按需修改。伪码例子：while(keepAdding){....; ttsEntrance.add(nextBytes); ...;}
	 */
	protected void repeatAddBytesRequest() {
		while (this.keepAdding) {
			// 方法一：
			// 建议每次添加1句完整的话，便于合成后语音的完整。较理想的情况：每次添加的文字长度小于50（不超过3句话）。
			// 每次调用add方法时添加的文字长度不限，由用户自行控制。本示例中，每次读取一行文字：
			// List<String> list = this.readLines(this.textFile);
			// for (int i = 0; i < list.size(); i++) {
			// ttsEntrance.add(list.get(i), "session-id-line-" + i);
			// sleepSomeTime(125); // 避免发的太快。
			// }

			// 方法二，使用SDK提供的行和语句的自动切分方案：
			File file = new File(this.textFile);
			ttsEntrance.add(file, "session-id-aysn");

			this.stop(); // 为方便演示本实例，不做循环发送，所以在此停止。
		}
	}

	// private List<String> readLines(String file) {
	// List<String> list = new ArrayList<String>();
	// BufferedReader in = null;
	// try {
	// in = new BufferedReader(new InputStreamReader(new FileInputStream(new File(file))));
	// String line = null;
	// while ((line = in.readLine()) != null)
	// list.add(line);
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// in.close();
	// } catch (Exception e) {
	// // ignore;
	// }
	// }
	// return list;
	// }
	//
	// private void sleepSomeTime(long millis) {
	// try {
	// Thread.sleep(millis);
	// } catch (InterruptedException e) {
	// // ignore
	// }
	// }
}

// --------------------------------------------------------------------------------------------------------------
/**
 * 用户自己写的回调Handler.被NotifyService服务线程（可理解为用户线程B）调用。
 */
class MyResponseHandler implements FlowHandler {

	private int handlerId;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public MyResponseHandler(int handlerId) {
		this.handlerId = handlerId;
	}

	/**
	 * 回复数据通过此方法通知到用户。
	 */
	@Override
	public void onUpdate(Object... params) {
		TtsResponse response = (TtsResponse) params[0];
		// Your own logic.
		String filePath = "logs/handler_" + this.handlerId + "_result_" + response.getSessionId() + ".pcm";
		this.saveResponseToFile(response.getResponseBytes(), filePath);
		System.out.println(sdf.format(new Date()) + " Handler_" + this.handlerId + ", response length: "
				+ response.getResponseBytes().length + ", result pcm saved at -->: " + filePath);

		// 可以查看延迟（未核验，仅供参考）。
		this.printDelay(response);
	}

	private void saveResponseToFile(byte[] response, String filePath) {
		try {
			new File(filePath).getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(filePath, true);
			out.write(response);
			out.close();
		} catch (IOException e) {
			System.err.println("Failed save data to: " + filePath + ", error: " + e.getMessage());
		}
	}

	/**
	 * 查看和打印延迟信息。延迟统计方法与实时语音Java SDK基本一样，但还没有做细节研究，用户可简单浏览，或先行跳过。
	 * 
	 * <pre>
	 * 实例如下：
	 * write delay: 103 ms, node delay: 103 ms, notify delay: 105 ms. 
	 * 分别表示：发送延迟、节点延迟、通知延迟。
	 * 
	 * 延迟含义解释：
	 * WriteDelay：    数据收发和网络延迟+解析Delay(1-2ms)。 
	 * NodeDelay：      数据滞留延迟 + WriteDelay。   即：从客户add完成1个分片数据开始，至分片结果收到为止，期间总的时间消耗。
	 * NotifyDelay： NodeDelay + 客户onHander Delay(处理回复耗时)。此值若大于NodeDelay且在增长，会导致Response堆积，最终内存溢出。
	 * </pre>
	 */
	private void printDelay(TtsResponse ttsResponse) {
		TimeStat timeStat = ttsResponse.getTimeStat();
		System.out.println(sdf.format(new Date()) + " write delay: " + timeStat.getWriteDelay() + " ms, node delay: "
				+ timeStat.getNodeDelay() + " ms, notify delay: " + timeStat.getNotifyDelay() + " ms.");
	}
}
