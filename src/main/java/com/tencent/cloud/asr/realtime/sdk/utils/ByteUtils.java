package com.tencent.cloud.asr.realtime.sdk.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * 与byte有关的工具类。提供了若干操作byte的工具函数。
 * </p>
 * <p>
 * <b>线程安全</b> 该类线程安全
 * </p>
 * 
 * @author Scofield, iantang
 * @version 1.0
 */
public class ByteUtils {

	/**
	 * 返回一个byte[]中指定的一部分。为了性能考虑，未对参数进行任何验证。
	 * 
	 * @param bs
	 *            原byte[]
	 * @param startIndex
	 *            开始索引
	 * @param length
	 *            要拷贝的字节长度
	 * @return byte[]中指定的一部分
	 * @see System#arraycopy(Object, int, Object, int, int)
	 */
	public static byte[] subBytes(byte[] bs, int startIndex, int length) {
		byte[] sub = new byte[length];
		System.arraycopy(bs, startIndex, sub, 0, length);
		return sub;
	}

	/**
	 * 合并两个byte[]
	 * 
	 * @param bytes1
	 *            第一个byte[]
	 * @param bytes2
	 *            第二个byte[]
	 * @return 合并后的数组
	 * @throws IllegalArgumentException
	 *             如果任一参数为null
	 */
	public static byte[] concat(byte[] bytes1, byte[] bytes2) {
		byte[] target = new byte[bytes1.length + bytes2.length];
		System.arraycopy(bytes1, 0, target, 0, bytes1.length);
		System.arraycopy(bytes2, 0, target, bytes1.length, bytes2.length);
		return target;
	}

	/**
	 * 将一个数组切分成很多个小的数组，数组在在指定的范围中随机确定。
	 */
	public static List<byte[]> subToSmallBytes(byte[] bs, int minLength, int maxLength) {
		int length = bs.length;
		if (maxLength > length)
			maxLength = length;
		List<byte[]> list = new ArrayList<byte[]>();
		int posi = 0;
		while (posi < length) {
			int randomLen = getRandomValue(minLength, maxLength);
			if (posi + randomLen > length)
				randomLen = length - posi;
			list.add(subBytes(bs, posi, randomLen));
			posi += randomLen;
		}
		return list;
	}

	/**
	 * 使用流的方式将文件读取一遍，然后切分成小的数组返回，数组大小为一个固定的值。
	 * 
	 * 切分大文件时会比{@link ByteUtils#subToSmallBytes(byte[], int, int)}方法快一点。
	 * 
	 * @param subLen
	 *            切成指定的大小。
	 */
	public static List<byte[]> subToSmallBytes(File file, int subLen) {
		List<byte[]> list = new ArrayList<byte[]>();
		InputStream inputStream = null;
		int available = 0, readLength = 0;
		byte[] subBytes = new byte[subLen];
		try {
			inputStream = new FileInputStream(file);
			available = inputStream.available();
			while (available > 0) {
				subBytes = new byte[subLen]; // 每次使用新的字节数组，避免add到缓存中的数组是同一条，造成异常。
				readLength = inputStream.read(subBytes);
				if (readLength == subLen) {
					list.add(subBytes);
				} else if (readLength > 0) {
					list.add(ByteUtils.subBytes(subBytes, 0, readLength));
				}
				available = inputStream.available();
			}
		} catch (IOException e) {
			System.err.println("Unexpected IOException: " + e.getMessage());
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				// ignore
			}
		}
		return list;
	}

	private static int getRandomValue(int minLength, int maxLength) {
		Random random = new Random();
		return random.nextInt(maxLength - minLength) + minLength;
	}

	public static byte[] inputStream2ByteArray(String filePath) {
		File file = new File(filePath);
		return inputStream2ByteArray(file);
	}

	public static byte[] inputStream2ByteArray(File file) {
		InputStream in;
		try {
			in = new FileInputStream(file);
			byte[] data = toByteArray(in);
			in.close();
			return data;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static byte[] toByteArray(InputStream in) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		try {
			while ((n = in.read(buffer)) != -1) {
				out.write(buffer, 0, n);
			}
			return out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*public static void main(String[] args) {
		byte[] bs = inputStream2ByteArray("D:/111.txt");
		List<byte[]> list = subToSmallBytes(bs, 5, 15);
		for (byte[] sub : list) {
			System.out.print(new String(sub));
		}
	}*/
}
