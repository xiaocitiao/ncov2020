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
package com.tencent.cloud.asr.realtime.sdk.utils;

import java.util.Random;

public class StringTools {

	// 定义一个字符集（A-Z，a-z，0-9）即62位；用于生成随机字符串
	private static final String RANDOM_CHARS = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";

	/**
	 * 获取指定长度的随机字符串。包含字符：a--z; A--Z; 0--9。
	 */
	public static String getRandomString(int length) {
		// 由Random生成随机数
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		// 长度为几就循环几次
		for (int i = 0; i < length; ++i) {
			// 产生0-61的数字
			int number = random.nextInt(RANDOM_CHARS.length());
			// 将产生的数字通过length次承载到sb中
			sb.append(RANDOM_CHARS.charAt(number));
		}
		// 将承载的字符转换成字符串
		return sb.toString();
	}
}
