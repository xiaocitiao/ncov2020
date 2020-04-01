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
package com.tencent.cloud.asr.realtime.sdk;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.tencent.cloud.asr.realtime.sdk.asyn_sender.ReceiverEntrance;
import com.tencent.cloud.asr.realtime.sdk.cache_handler.FlowHandler;
import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.config.AsrPersonalConfig;
import com.tencent.cloud.asr.realtime.sdk.model.enums.EngineModelType;
import com.tencent.cloud.asr.realtime.sdk.model.enums.ResponseEncode;
import com.tencent.cloud.asr.realtime.sdk.model.enums.SdkRole;
import com.tencent.cloud.asr.realtime.sdk.model.enums.VoiceFormat;
import com.tencent.cloud.asr.realtime.sdk.model.response.TimeStat;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;
import com.tencent.cloud.asr.realtime.sdk.utils.ByteUtils;
import com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig;
//import com.tencent.cloud.asr.realtime.sdk.model.response.VadResponse;
//import com.tencent.cloud.asr.realtime.sdk.model.response.VadResult;
//import com.tencent.cloud.asr.realtime.sdk.config.AsrBaseConfig;
//import com.tencent.cloud.asr.realtime.sdk.utils.JacksonUtil;

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
 * 详见本VoiceTask类中的run()方法。
 * 
 * @author iantang
 * @version 1.0
 */
public class RasrAsynRequestSample {

	static {
		initBaseParameters();
	}

	private int threadNumber = 1;
	private String voiceFile = "test_wav/8k/8k.wav";

	private List<VoiceTask> taskList = new ArrayList<VoiceTask>();

	public static void main(String[] args) {
		RasrAsynRequestSample rasrRequestSample = new RasrAsynRequestSample();
		rasrRequestSample.setArguments(args);
		rasrRequestSample.start();

		sleepSomeTime(600000); // 10分钟后停止示例程序。
		rasrRequestSample.stop();
		System.exit(0);
	}

	/**
	 * 根据需求启动多个任务，每个任务都独立运行，互不干扰。
	 */
	public void start() {
		for (int i = 1; i <= this.threadNumber; i++) {
			VoiceTask voiceTask = new VoiceTask(i, this.voiceFile);
			this.taskList.add(voiceTask);
			voiceTask.start();
			sleepSomeTime(20);
		}
	}

	/**
	 * 停止全部任务。
	 */
	public void stop() {
		for (VoiceTask voiceTask : this.taskList) {
			voiceTask.stop();
		}
	}

	/**
	 * 初始化基础参数, 请将下面的参数值配置成你自己的值。
	 * 
	 * 参数获取方法可参考： <a href="https://cloud.tencent.com/document/product/441/6203">签名鉴权 获取签名所需信息</a>
	 */
	private static void initBaseParameters() {
		// Required
		// AsrBaseConfig.appId = "YOUR_APP_ID_SET_HERE";
		// AsrBaseConfig.secretId = "YOUR_SECRET_ID";
		// AsrBaseConfig.secretKey = "YOUR_SECRET_KEY";

		// optional，根据自身需求配置值
		AsrInternalConfig.setSdkRole(SdkRole.VAD); // VAD版用户请务必赋值为 SdkRole.VAD
		AsrPersonalConfig.responseEncode = ResponseEncode.UTF_8;
		AsrPersonalConfig.engineModelType = EngineModelType._8k_0;
		AsrPersonalConfig.voiceFormat = VoiceFormat.wav;

		// optional, 可忽略
		AsrGlobelConfig.CUT_LENGTH = 4096; // 每次发往服务端的语音分片的字节长度，8K语音建议设为4096,16K语音建议设为8192。
		// AsrGlobelConfig.NEED_VAD = 0; // 是否要做VAD，默认为1，表示要做。线上用户不适用，请忽略。
		// AsrGlobelConfig.NOTIFY_ALL_RESPONSE = true; // 是否回调每个分片的回复。如只需最后的结果，可设为false。
		// AsrBaseConfig.PRINT_CUT_REQUEST = true; // 打印每个分片的请求，用于测试。
		// AsrBaseConfig.PRINT_CUT_RESPONSE = true; // 打印中间结果，用于测试，生产环境建议设为false。
		// 默认使用自定义连接池，连接数可在AsrGlobelConfig中修改，更多细节参数，可直接修改源码HttpPoolingManager.java,然后自行打Jar包。
		// AsrGlobelConfig.USE_CUSTOM_CONNECTION_POOL = true;
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
	 * 比如这个命令传入了4个参数： java -jar realAsrSdk_run.jar 10 test_wav/8k/8k_19s.wav 8k false
	 * 
	 * 详情可见：out_runnable_jar/command_reference.txt
	 */
	private void setArguments(String[] args) {
		if (args.length > 0)
			this.threadNumber = Integer.parseInt(args[0]); // 使用传入的参数赋值线程个数
		if (args.length > 1) {
			this.voiceFile = args[1];
			this.checkSetVoiceFormat(this.voiceFile);
		}
		if (args.length > 2) {
			AsrPersonalConfig.engineModelType = EngineModelType.parse(args[2]);
			if (AsrPersonalConfig.engineModelType == EngineModelType._16k_0)
				AsrGlobelConfig.CUT_LENGTH = 8192; // 16K语音 也设置成每秒发4次请求，优化演示效果。
		}
		if (args.length > 3)
			AsrGlobelConfig.NOTIFY_ALL_RESPONSE = Boolean.parseBoolean(args[3]);
	}

	private void checkSetVoiceFormat(String voiceFile) {
		int index = voiceFile.lastIndexOf(".");
		if (index == -1)
			return;
		String formatName = voiceFile.substring(index + 1).trim().toLowerCase();
		AsrPersonalConfig.voiceFormat = VoiceFormat.parse(formatName);
	}
}

// --------------------------------------------------------------------------------------------------------------
/**
 * 单个任务对象示例。包含了服务线程的创建、启动和关闭方法。
 * 
 * @author iantang
 * @version 1.0
 */
class VoiceTask {

	private ReceiverEntrance receiverEntrance;
	private int taskId;
	private String voiceFile;
	private VoiceAddingTask voiceAddingTask;

	public VoiceTask(int taskId, String voiceFile) {
		this.taskId = taskId;
		this.voiceFile = voiceFile;
	}

	/**
	 * 创建和启动服务线程。包括：数据添加线程、发送线程、通知线程。
	 */
	public void start() {
		// 新建一个服务
		this.receiverEntrance = new ReceiverEntrance(taskId);
		// 启动服务
		this.receiverEntrance.start();
		// 注册N个回调Handler
		this.receiverEntrance.registerReponseHandler(new MyResponseHandler(this.taskId));
		// 开始添加数据
		this.voiceAddingTask = new VoiceAddingTask(this.receiverEntrance, voiceFile);
		this.voiceAddingTask.start();

		// 10秒后停止任务/关闭服务。如需一直使用，则不要调用它。
		/*this.sleepSomeTime(10000);
		this.stop();*/
	}

	public void stop() {
		this.voiceAddingTask.stop();
		this.receiverEntrance.stopService();
	}
}

// --------------------------------------------------------------------------------------------------------------
/**
 * 添加语音数据的任务类。模拟：独立线程，持续将语音片段（字节数据）添加到缓存。
 * 
 * @author iantang
 * @version 1.0
 */
class VoiceAddingTask {

	private ReceiverEntrance receiverEntrance;
	private String voiceFile;
	private boolean keepAdding = true;

	public VoiceAddingTask(ReceiverEntrance receiverEntrance, String voiceFile) {
		this.receiverEntrance = receiverEntrance;
		this.voiceFile = voiceFile;
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
	 * 用户线程A：持续将语音片段（字节数据）添加到缓存。具体的发送和通知动作由其它线程负责。
	 * 
	 * 本方法仅作示例用途，用户可按需修改。伪码例子：while(keepAdding){....; receiverEntrance.add(nextBytes); ...;}
	 */
	protected void repeatAddBytesRequest() {
		while (this.keepAdding) {
			// 方法1：流式请求模拟示例。不断追加小段数据，在语音结束后，才调用voiceEnd方法标识结尾。
			// 下面只是示例，每次调用add方法时添加的字节数组的长度可由用户根据实际情况自行控制，大小不限。
			int subLength = AsrGlobelConfig.CUT_LENGTH / 2; // 在当前示例中，此值为：2048，或4096
			List<byte[]> list = ByteUtils.subToSmallBytes(new File(this.voiceFile), subLength);
			for (int i = 0; i < list.size() - 1; i++) {
				receiverEntrance.add(list.get(i));
				sleepSomeTime(125); // 对于8K的语音，每秒发出2048*8 = 16KB数据，与实际相符。
			}
			receiverEntrance.add(list.get(list.size() - 1)); // add last bytes
			receiverEntrance.voiceEnd();

			// 方法2：添加一整句话，且标识结尾：
			/*byte[] content = ByteUtils.inputStream2ByteArray("test_wav/8k.wav");
			// 下面的两句话，可以用 receiverEntrance.add(content, true);来代替。
			receiverEntrance.add(content);
			receiverEntrance.voiceEnd();*/

			this.stop(); // 为方便演示本实例，不做循环发送，所以在此停止。
		}
	}

	/**
	 * 停止添加线程（用户线程A）
	 */
	public void stop() {
		this.keepAdding = false;
	}

	private void sleepSomeTime(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// ignore
		}
	}
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
		// VadResponse response = (VadResponse) params[0]; // Vad版用户请用此行代替下面一行
		VoiceResponse response = (VoiceResponse) params[0];

		// Your own logic.
		System.out.println(sdf.format(new Date()) + " Handler_" + this.handlerId + ", received response -->: "
				+ response.getOriginalText());
		/*System.out.println(JacksonUtil.toJsonString(voiceResponse));*/

		// 可以查看延迟。
		this.printDelay(response);

		// 可以提取出当前Vad断句回复。适用于Vad版用户，线上用户请忽略。
		/*if (response.getResultList() != null) {
			for (VadResult vadResult : response.getResultList()) {
				System.out.println("Received vad response: " + vadResult.getVoiceTextStr());
			}
		}*/
	}

	/**
	 * 查看和打印延迟信息。延迟统计方法可从项目docs目录中查看，或浏览下面的含义解释。
	 * 
	 * <pre>
	 * 实例如下：
	 * <<<End_Cut>>> write delay: 103 ms, node delay: 103 ms, notify delay: 105 ms. Pre average write: 101 ms, node: 102 ms.
	 * 分别表示：发送延迟、节点延迟、通知延迟。前面N个分片的平均发送、平均节点延迟。
	 * 
	 * 如需测试语音发完后多久能收完全部识别结果，可从：“<<<End_Cut>>>” 中的notify delay获得参考。建议以write Delay作为服务性能考量。
	 * 
	 * 延迟含义解释：
	 * WriteDelay：    数据收发和网络延迟+解析Delay(1-2ms)。 
	 * NodeDelay：      数据滞留延迟 + WriteDelay。   即：从客户add完成1个分片数据开始，至分片结果收到为止，期间总的时间消耗。
	 * NotifyDelay： NodeDelay + 客户onHander Delay(处理回复耗时)。此值若大于NodeDelay且在增长，会导致Response堆积，最终内存溢出。
	 * </pre>
	 */
	private void printDelay(VoiceResponse voiceResponse) {
		TimeStat timeStat = voiceResponse.getTimeStat();
		if (voiceResponse.isEndCut()) {
			this.printDelay("<<<End_Cut>>>", timeStat);
		} else {
			this.printDelay("<<<Middle_Cut>>>", timeStat);
		}
	}

	private void printDelay(String cutType, TimeStat timeStat) {
		System.out.println(sdf.format(new Date()) + " " + cutType + " write delay: " + timeStat.getWriteDelay()
				+ " ms, node delay: " + timeStat.getNodeDelay() + " ms, notify delay: " + timeStat.getNotifyDelay()
				+ " ms. Pre average write: " + timeStat.getPreAverageWriteDelay() + " ms, node: "
				+ timeStat.getPreAverageNodeDelay() + " ms.");
	}
}
