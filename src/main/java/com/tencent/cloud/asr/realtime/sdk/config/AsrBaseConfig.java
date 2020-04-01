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
 * 用户的语音请求基本参数，仅需配置一次，全局使用。通常是注册腾讯云账号后，登录控制台，从“个人API秘钥”中获得。
 * 
 * 获取方法请查看： <a href="https://cloud.tencent.com/document/product/441/6203">签名鉴权 获取签名所需信息</a>
 * 
 * 请用户务必将自己的各项参数值赋值到本类对应变量中。
 * 
 * @author iantang
 * @version 1.0
 */
public class AsrBaseConfig {

	public static String secretId = "AKID31NbfXbpBLJ4kGJrytc9UfgVAlGltJJ8";

	public static String secretKey = "kKm26uXCgLtGRWVJvKtGU0LYdWCgOvGP";

	public static String appId = "1255628450";

	/** 是否打印分片请求到控制台 */
	public static boolean PRINT_CUT_REQUEST = false;

	/** 是否打印分片回复到控制台 ，可用于验证SDK收到了中间结果 */
	public static boolean PRINT_CUT_RESPONSE = false;

}
