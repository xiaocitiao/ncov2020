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
package com.tencent.cloud.asr.realtime.sdk.config;

/**
 * 保存项目的全局参数配置，用户可按需修改。
 * 
 * @author iantang
 * @version 1.0
 */
public class AsrGlobelConfig {

	/**
	 * 语音切片字节长度，需要小于200000。不建议设置成太大或者太小的值。
	 * 
	 * <pre>
	 * 原因是： 
	 * 1. 如果设置太小，则发出的分片数量特别多，会影响最终的识别速度；
	 * 2. 如果设置太大，则中间结果返回的相对较慢，总的识别速度也并不是最快。
	 * </pre>
	 */
	public static int CUT_LENGTH = 8192;

	/**
	 * 是否将每个分片结果全都通知给监听者（客户）。
	 * 
	 * <pre>
	 * true：通知全部回复，收到的每个分片的回复，全部都会通过回调handler通知给用户。适用于每个结果都想要的客户。
	 * false：只通知尾包，只把每个Vad分段后的每句话的最终识别结果通知给用户。适用于只需要最终结果的客户。
	 * </pre>
	 * 
	 * 注意：如果另一个属性：returnType的值设置为TAILER，则本属性值建议设置成false， 因为中间的回复内容中不包含识别结果，没必要通知客户。
	 */
	public static boolean NOTIFY_ALL_RESPONSE = true;

	/**
	 * 是否需要做VAD，1：要做，0：不做。默认为1. 仅在AsrInternalConfig.SDK_ROLE=VAD时有效。
	 */
	public static int NEED_VAD = 1;

	// -------------------- Connection Pool Config begin -----------------------------
	// ------------- 如果您的实际并发线程数未超过600，可不用修改下面的内容 ---------------

	/** 是否使用自定义的Http连接池，默认使用。为false则会使用HttpClient的默认连接池 */
	public static boolean USE_CUSTOM_CONNECTION_POOL = true;

	/** 总的最大连接数 */
	public static int MAX_TOTAL = 1000;
	/** 默认的每个路由的最大连接数 */
	public static int DEFAULT_MAX_PER_ROUTE = 1000;
	/** 自定义的ASR路由的最大连接数 */
	public static int CUSTOM_MAX_PER_ROUTE = 1000;

	/** 连接超时后尝试重连的次数 */
	public static int RECONNECTION_TIMES = 3;

	// -------------------- Connection Pool Config end -----------------------------

	static {
		if (CUT_LENGTH < 0 || CUT_LENGTH > 200000) {
			System.err.println("cutlength is not vailed!");
			System.exit(-1);
		}
	}
}
