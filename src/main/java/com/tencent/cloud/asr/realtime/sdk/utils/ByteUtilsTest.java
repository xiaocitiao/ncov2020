package com.tencent.cloud.asr.realtime.sdk.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ByteUtilsTest {

	/**
	 * 测试subToSmallBytes(File file, int subLen)方法的功能是否正常。
	 * 
	 * 测试方法：将文件内容切分成多个小数组后，再拼装起来保存到新文件中。
	 * 
	 * 然后，用BeyondCompare等工具比较新文件与原始文件的内容是否完全相同。
	 * 
	 * 测试结果：通过
	 */
	public void testSubToSmallBytes() throws IOException {
		List<byte[]> list = ByteUtils.subToSmallBytes(new File("test_wav/8k.wav"), 8192);
		System.err.println(list.size());

		byte[] res = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			res = ByteUtils.concat(res, list.get(i));
		}
		OutputStream out = new FileOutputStream(new File("D:\\222.wav"));
		out.write(res);
		out.close();
	}

	public static void main(String[] args) throws IOException {
		ByteUtilsTest byteUtilsTest = new ByteUtilsTest();
		byteUtilsTest.testSubToSmallBytes();
	}

}
