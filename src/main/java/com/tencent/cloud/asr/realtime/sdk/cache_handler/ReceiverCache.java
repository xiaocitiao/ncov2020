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
package com.tencent.cloud.asr.realtime.sdk.cache_handler;

import static com.tencent.cloud.asr.realtime.sdk.config.AsrGlobelConfig.CUT_LENGTH;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.tencent.cloud.asr.realtime.sdk.config.AsrInternalConfig;
import com.tencent.cloud.asr.realtime.sdk.model.request.RasrBytesRequest;
import com.tencent.cloud.asr.realtime.sdk.utils.ByteUtils;

/**
 * 源数据缓存管理。
 * 
 * 有需要时可再实现多线程同时发送请求： 将本类中的Queue改成多个Queue，分配给多个子线程持有。收数据时按照voiceId分配给某个Queue。
 * 
 * @author iantang
 * @version 1.0
 */
public class ReceiverCache {

	private BlockingQueue<RasrBytesRequest> requestQueue = new LinkedTransferQueue<RasrBytesRequest>();
	private RasrBytesRequest currentRequest = this.createBytesRequest();
	private AtomicInteger queueSize = new AtomicInteger(0);

	/**
	 * 异步增加声音数据。
	 * 
	 * @param content
	 *            字节数组
	 * 
	 * @return true表示成功。false表示添加失败，通常是因为调用太频繁，内存积压太多数据（暂定：超过1万条）未被发出造成。
	 */
	public synchronized boolean add(byte[] content) {
		if (this.cacheFulled()) {// 避免缓存太多内存溢出。
			return false;
		}
		if (content == null)
			return false;
		// 按分片大小提前切分好大的数组:
		if (content.length > CUT_LENGTH) {
			this.cutAndAdd(content);
			return true;
		}
		this.currentRequest.add(content);
		if (this.currentRequest.lengthEnough()) {
			transferRequest();
		}
		return true;
	}

	/**
	 * 异步增加声音数据,且确保肯定添加到SDK的缓存中。
	 * 
	 * 如果当前缓存满了，会一直阻塞。
	 */
	public synchronized boolean addUntilSuccess(byte[] content) {
		this.waitCacheRelease();
		return this.add(content);
	}

	/**
	 * 判断临时缓存是否已经存满了数据。如果返回true则建议用户先停止执行Add()方法，直到此方法返回False。
	 * 
	 * @param content
	 *            字节数组
	 * 
	 * @return false表示缓存还没满，可以继续添加；true表示缓存满了，通常是因为调用add太频繁，内存积压太多数据（默认为：超过1万条）未被发出造成。
	 */
	public synchronized boolean cacheFulled() {
		return (this.queueSize.intValue() >= AsrInternalConfig.RECEIVER_CACHE_QUEUE_MAX_SIZE);
	}

	/**
	 * 标记当前声音数据到达一句的结尾。
	 */
	public synchronized void voiceEnd() {
		this.currentRequest.setEndReceived();
		this.transfer(this.currentRequest);
		this.currentRequest = this.createBytesRequest();
	}

	/**
	 * 异步增加声音数据。添加数据的同时标识此数据是否为一句语音的结尾。
	 * 
	 * @param content
	 *            字节数组
	 * @param endFlag
	 *            结束标记
	 * 
	 * @return true表示成功。false表示添加失败，通常是因为调用太频繁，内存积压太多数据（暂定：超过1万条）未被发出造成。
	 */
	public synchronized boolean add(byte[] content, boolean endFlag) {
		boolean result = this.add(content);
		if (!result) {
			return false;
		}
		if (endFlag)
			this.voiceEnd();
		return result;
	}

	/**
	 * 异步增加声音数据,且确保肯定添加到SDK的缓存中。
	 * 
	 * 如果当前缓存满了，会一直阻塞。
	 */
	public synchronized boolean addUntilSuccess(byte[] content, boolean endFlag) {
		this.waitCacheRelease();
		return this.add(content, endFlag);
	}

	public RasrBytesRequest takeNext() {
		RasrBytesRequest firstPacket = null;
		try {
			firstPacket = this.requestQueue.take();
			this.queueSize.getAndDecrement();
		} catch (InterruptedException e) {
			// e.printStackTrace();
			System.err.println("Receiver Cache take next message has been Interrupted! will sotp request.");
			// logger.error("Receiver Service Cache getNext message has been Interrupted!", e);
		}
		return firstPacket;
	}

	/**
	 * 看看当前缓存中还有没有剩余未处理的数据。此方法目前未被使用。
	 * 
	 * <pre>
	 * 历史缘由备注（可忽略此段）
	 * 原计划的用途是： 在RequestService.java的构造方法中，将本对象赋值给AsynRequestSender对象，
	 * 然后在send方法中使用: 当前的最后一个分片刚好等于CUT_LENGTH时，判断是否可以发出。
	 * 后来发现这样做也不是根本办法：
	 * 符合发送长度的最后分片终究还是有较大概率滞留到lastCachedBytes变量中，然后在下一次执行send方法时才被发出。
	 * 所以，取消了本方法的使用。改用测试后发现实际可行的另一方案：每次都及时发出数据，
	 * 发送最后的voiceEnd时如果当前已无缓存（lastCachedBytes刚好null），则消息体填充空的字节数组（长度为2的倍数)。
	 * </pre>
	 */
	public boolean hasRemaining() {
		return this.currentRequest.containsData() || this.queueSize.intValue() > 0;
	}

	/**
	 * 尝试取得下一份请求对象且不出队列。目前未用到。
	 * 
	 * 原计划也是想用来判断是否能发送最后一个分片，发现不可行，故弃之。
	 */
	public RasrBytesRequest touchNext() {
		return this.requestQueue.peek();
	}

	/**
	 * 获取当前缓存的数据条数
	 */
	public int getCachedQueueSize() {
		return queueSize.get();
	}

	/**
	 * 清空缓存
	 */
	public void clear() {
		this.requestQueue.clear();
		this.queueSize = new AtomicInteger(0);
		this.currentRequest = this.createBytesRequest();
	}
	
	public RasrBytesRequest getCurrentRequest() {
		return currentRequest;
	}
	
	protected RasrBytesRequest createBytesRequest() {
		return new RasrBytesRequest();
	}
	
	protected RasrBytesRequest createBytesRequest(RasrBytesRequest previous) {
		return new RasrBytesRequest(previous);
	}

	private void cutAndAdd(byte[] current) {
		int posi = 0;
		while (current.length - posi >= CUT_LENGTH) {
			this.currentRequest.add(ByteUtils.subBytes(current, posi, CUT_LENGTH));
			this.transferRequest();
			posi = posi + CUT_LENGTH;
		}
		if (posi >= current.length)
			return;
		// 添加剩下的最后一块
		this.currentRequest.add(ByteUtils.subBytes(current, posi, current.length - posi));
	}

	private void transferRequest() {
		this.transfer(this.currentRequest);
		this.currentRequest = this.createBytesRequest(this.currentRequest);
	}

	private void transfer(RasrBytesRequest rasrBytesRequest) {
		try {
			rasrBytesRequest.setMessageTime(System.currentTimeMillis());
			this.requestQueue.put(rasrBytesRequest);
			this.queueSize.getAndIncrement();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("Receiver Cache transfer voiceId: " + rasrBytesRequest.getVoiceId()
					+ " failed, queue interrupted! ");
			// logger.error("Receiver Service Cache transfer voiceId: " + rasrBytesRequest.getVoiceId()
			// + " has been Interrupted!", e);
		}
	}

	private void waitCacheRelease() {
		while (this.cacheFulled()) {
			try {
				Thread.sleep(250);
			} catch (Exception e) {
				// ignore
			}
		}
	}
}
