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
package com.tencent.cloud.asr.realtime.sdk.asyn_sender;

import java.util.Random;

import com.tencent.cloud.asr.realtime.sdk.cache_handler.FlowHandler;
import com.tencent.cloud.asr.realtime.sdk.cache_handler.ReceiverCache;
import com.tencent.cloud.asr.realtime.sdk.model.response.VoiceResponse;

/**
 * 程序请求入口，异步方法接收数据，并提供回复获取方法。
 * 
 * <p>
 * <b>线程安全</b> 做了基本处理，初步来看线程安全。
 * 
 * @author iantang
 * @version 1.0
 */
public class ReceiverEntrance {

	private ReceiverCache receiverCache;
	private RequestService requestService;
	private NotifyService notifyService;

	/**
	 * 构造函数。新建一个服务，将相关的各类服务对象都创建出来。
	 */
	public ReceiverEntrance() {
		this(new Random().nextInt(10000));
	}

	/**
	 * 构造函数。新建一个服务，将相关的各类服务对象都创建出来。
	 * 
	 * @param serviceId
	 *            服务Id号，打印日志用，无特殊要求。
	 */
	public ReceiverEntrance(long serviceId) {
		this.receiverCache = this.createReceiverCache();
		this.notifyService = new NotifyService(serviceId);
		this.requestService = new RequestService(this.receiverCache, this.notifyService, serviceId);
	}

	/**
	 * 将异步服务启动起来。对于当前对象，仅需调用一次。
	 */
	public void start() {
		this.requestService.start();
		this.notifyService.start();
	}

	/**
	 * 增加声音数据到缓存。
	 * 
	 * @param content
	 *            字节数组
	 * 
	 * @return true表示成功。false表示添加失败，通常是因为调用太频繁，内存积压太多数据（暂定：超过1万条）未被发出造成。
	 */
	public boolean add(byte[] content) {
		return this.receiverCache.add(content);
	}

	/**
	 * 增加声音数据到缓存,且确保肯定添加到SDK的缓存中。
	 * 
	 * 如果当前缓存满了，会一直阻塞。
	 * 
	 * @return 通常会返回true。用户可忽略此返回值。
	 */
	public boolean addUntilSuccess(byte[] content) {
		return this.receiverCache.addUntilSuccess(content);
	}

	/**
	 * 标记当前声音数据到达一句的结尾。
	 */
	public void voiceEnd() {
		this.receiverCache.voiceEnd();
	}

	/**
	 * 增加声音数据到缓存且标识当前这份数据是否为一句语音的结尾。
	 * 
	 * @param content
	 *            字节数组
	 * @param endFlag
	 *            结束标记
	 * 
	 * @return true表示成功。false表示添加失败，通常是因为调用太频繁，内存积压太多数据（暂定：超过1万条）未被发出造成。
	 */
	public boolean add(byte[] content, boolean endFlag) {
		return this.receiverCache.add(content, endFlag);
	}

	/**
	 * 增加声音数据到缓存且标识当前这份数据是否为一句语音的结尾, 且确保肯定添加到SDK的缓存中。
	 * 
	 * 如果当前缓存满了，会一直阻塞。
	 * 
	 * @return 通常会返回true。用户可忽略此返回值。
	 */
	public boolean addUntilSuccess(byte[] content, boolean endFlag) {
		return this.receiverCache.addUntilSuccess(content, endFlag);
	}

	/**
	 * 判断临时缓存是否已经存满了数据。如果返回true则建议用户先停止执行Add方法，直到此方法返回False。
	 * 
	 * @param content
	 *            字节数组
	 * 
	 * @return false表示缓存还没满，可以继续添加；true表示缓存满了，通常是因为调用add太频繁，内存积压太多数据（暂定：超过1万条）未被发出造成。
	 */
	public boolean cacheFulled() {
		return this.receiverCache.cacheFulled();

	}

	/**
	 * 回调接口注册。即：注册接收回复的处理类，回复数据收到后，处理类中的onUpdate方法会被调用。
	 * 
	 * @param flowHandler
	 *            用户自己的实现了flowHandler接口的类。
	 */
	public void registerReponseHandler(FlowHandler flowHandler) {
		this.notifyService.register(flowHandler);
	}
	
	/**
	 * 获取当前最后一次从语言服务器收到的回复。
	 * 
	 * 未发出End标记（即未调用voiceEnd方法）之前可通过本方法查看最后收到的内容。
	 */
	public VoiceResponse getLastResponse() {
		return this.requestService.getLastResponse();
	}
	
	/**
	 * 获取RequestService对象，TTS SDK等子项目会用到这个方法。。
	 */
	public RequestService getRequestService() {
		return requestService;
	}

	/**
	 * 获取当前缓存的数据条数。注意：此缓存中每一条数据的长度与用户调用add方法时传入的字节数组长度有关。
	 * 
	 * 此数值如果逐渐增大，说明添加的语音速度快于正常的语速，占用的系统内存会逐渐增加。
	 * 
	 * 缓存最大值目前默认为1000，可修改：AsrGlobelConfig.RECEIVER_CACHE_QUEUE_MAX_SIZE。
	 * 
	 * <pre>
	 * 一条数据由一个长数组、或者多个短数组组成：
	 * 用户传入的byte[]数组长度大于AsrGlobelConfig.CUT_LENGTH时，新建一条数据，包含此数组（和前一个数组），并将它加入缓存队列；
	 * 用户传入的byte[]数组长度小于AsrGlobelConfig.CUT_LENGTH时，将此数组追加到当前现有数据中，此数据暂不加入缓存队列。
	 * </pre>
	 */
	public int getCachedQueueSize() {
		return this.receiverCache.getCachedQueueSize();
	}

	/**
	 * 获取通知队列中待通知的VoiceResponse对象的个数。此值较大时说明用户处理回复不够快，数据有堆积。
	 */
	public int getNotifyQueueSize() {
		return this.notifyService.getNotifyQueueSize();
	}

	/**
	 * 停止服务清空缓存。服务停止后，不会再占用资源。包括：停止异步发送线程 和 通知线程
	 */
	public void stopService() {
		this.notifyService.stop();
		this.requestService.stop();
		this.receiverCache.clear();
	}
	
	protected ReceiverCache createReceiverCache() {
		return new ReceiverCache();
	}
}
