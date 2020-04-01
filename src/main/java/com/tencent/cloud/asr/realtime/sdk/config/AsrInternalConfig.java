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

import com.tencent.cloud.asr.realtime.sdk.model.enums.ReturnType;
import com.tencent.cloud.asr.realtime.sdk.model.enums.SdkRole;

/**
 * 保存项目内部自定义的全局参数配置，通常由SDK维护人员来修改，大多数时候，SDK使用者都可忽略它们。
 * 
 * @author iantang
 * @version 1.0
 */
public class AsrInternalConfig {

	/**
	 * 当前SDK的角色。如果是用来连接VAD版的识别服务器，则设置为VAD。否则应设置为ONLINE，连接当前的线上服务。
	 * 
	 * 其它配置都依赖它，请务必配置正确，或赋值正确。
	 */
	private static SdkRole SDK_ROLE = SdkRole.ONLINE;

	/**
	 * 腾讯云项目 ID，目前选默认值即可。
	 */
	public static int PROJECT_ID = 1013976;

	/**
	 * 实时语音服务URL地址。选用默认值即可。
	 */
	public static String REAL_ASR_URL;
	/**
	 * 签名时使用的URL。此参数值仅用于腾讯内部测试。
	 */
	public static String SIGN_URL;

	static {
		initUrl();
	}

	/**
	 * 子服务类型。0：全文转写；1：实时流式识别。目前选用默认值即可。
	 */
	public static int SUB_SERVICE_TYPE = 1;

	/**
	 * 返回类型： 实时返回，或者尾包返回。考虑到节省的带宽资源很有限，所以建议用户选默认值。
	 * 
	 * <pre>
	 * 区别如下：
	 * REALTIME_FOLLOW：实时返回。占用的资源和带宽会多一点，但能立刻知道每次分片请求后的结果。
	 * TAILER：尾包返回。占用的资源和带宽相对少一些。只在最后一条分片请求的回复中附上结果。
	 * </pre>
	 * 
	 * 由于客户可能会想要分片识别过程中的中间结果，所以目前默认设置为：REALTIME_FOLLOW
	 */
	public static ReturnType returnType = ReturnType.REALTIME_FOLLOW;

	/**
	 * 临时缓存队列的长度，暂定位10000。
	 * 
	 * <pre>
	 * 用户调用ReceiverEntrance.add方法往SDK输入字节数组时，字节数组会被临时缓存起来，单独由处理线程不断读取和处理。
	 * 通过：receiverCache.getCachedQueueSize()方法可以获取到此队列中缓存的数据条数。正常情况下，条数通常为个位数，大多数时候为0.
	 * 
	 * 如果此值变大，可能的原因是：1. 用户add数据的速度超过了正常的语音速度（比如一次性add一个30秒的语音文件数据）；
	 * 2. 其它。比如：网络或服务器的原因。
	 * </pre>
	 */
	public static int RECEIVER_CACHE_QUEUE_MAX_SIZE = 10000;

	/**
	 * 判断当前SDK是否用于连接VAD版的识别服务器。
	 */
	public static boolean isVadRole() {
		return SDK_ROLE == SdkRole.VAD;
	}

	/**
	 * 设置当前SDK的角色类型：VAD版还是线上版，请务必设置准确。
	 */
	public static void setSdkRole(SdkRole sdkRole) {
		SDK_ROLE = sdkRole;
		initUrl();
	}

	private static void initUrl() {
		if (isVadRole()) {
			REAL_ASR_URL = "http://aairealtime.qcloud.com/asr/v1/";
			SIGN_URL = "http://aairealtime.qcloud.com/asr/v1/";
		} else {
			REAL_ASR_URL = "http://aai.qcloud.com/asr/v1/";
			SIGN_URL = "http://aai.qcloud.com/asr/v1/";
		}
	}
}
