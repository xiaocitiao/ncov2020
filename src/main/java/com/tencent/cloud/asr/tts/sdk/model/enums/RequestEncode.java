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
package com.tencent.cloud.asr.tts.sdk.model.enums;

import java.nio.charset.Charset;

/**
 * 传入的请求数据的编码
 * 
 * @author iantang
 * @version 1.0
 */
public enum RequestEncode {

	UTF_8(0, "utf-8"), GB2312(1, "gb2312"), GBK(2, "gbk"), BIG5(3, "BIG5");

	private int id;
	private String name;

	private RequestEncode(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Charset getCharset() {
		return Charset.forName(this.name);
	}

	public static RequestEncode parse(String charsetName) {
		switch (charsetName.toLowerCase()) {
		case "utf-8":
		case "utf_8":
			return UTF_8;
		case "gb2312":
			return GB2312;
		case "gbk":
			return GBK;
		case "big5":
			return BIG5;
		default:
			System.out.println("Unrecognized charsetName: " + charsetName + ", return UTF-8 as default.");
			return UTF_8;
		}
	}
}
