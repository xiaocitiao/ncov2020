package com.tencent.cloud.asr.realtime.sdk.model.response;

/**
 * 
 * 延迟统计对象。每个VoiceResponse对象都会持有一个本类对象。
 * 
 * @author iantang
 * @version 1.0
 */
public class TimeStat {

	/**
	 * 当前回复对应的请求数据被添加到缓存队列时的时间，相当于初始时间。<br>
	 * 可用于统计NodeDealy和NotifyDelay：客户发完数据后，总共经过多久能拿到结果。
	 */
	private long messageTime;

	/** 当前分片的请求回复耗时。最后分片的延迟也是它。 从发送请求前开始算起 */
	private long writeDelay;

	/** 当前分片数据的节点耗时，从数组被add开始算起。 */
	private long nodeDelay;

	/** VAD之后本段语音的前面全部分片的平均发送延迟 */
	private long preAverageWriteDelay;

	/** VAD之后本段语音的前面全部分片的平均节点延迟 */
	private long preAverageNodeDelay;

	/** 客户从add完数据到收到结果期间的耗时。只比nodeDela多了一个数据解析的时间（Json字符串解析成对象），正常情况下多1-2ms */
	private long notifyDelay;

	/**
	 * 是否为整段语音的结尾（注意不是VAD语音段落的结尾，需要调用ReceiverEntrance的voiceEnd()方法后，本变量才有机会为true）。
	 */
	private boolean isEndCut;

	public boolean isEndCut() {
		return isEndCut;
	}

	public void setEndCut(boolean isEndCut) {
		this.isEndCut = isEndCut;
	}

	public long getWriteDelay() {
		return writeDelay;
	}

	public void setWriteDelay(long writeDelay) {
		this.writeDelay = writeDelay;
	}

	/**
	 * 当前分片数据的节点耗时，比NotifyDelay少了结果解析的耗时。
	 */
	public long getNodeDelay() {
		return nodeDelay;
	}

	public void setNodeDelay(long nodeDelay) {
		this.nodeDelay = nodeDelay;
	}

	/**
	 * 本句（或本VAD段落）语音的前面全部分片的平均发送延迟
	 */
	public long getPreAverageWriteDelay() {
		return preAverageWriteDelay;
	}

	public void setPreAverageWriteDelay(long preAverageWriteDelay) {
		this.preAverageWriteDelay = preAverageWriteDelay;
	}

	/**
	 * 本句（或本VAD段落）语音的前面全部分片的平均节点延迟（不包含结果解析的时间）。
	 */
	public long getPreAverageNodeDelay() {
		return preAverageNodeDelay;
	}

	public void setPreAverageNodeDelay(long preAverageNodeDelay) {
		this.preAverageNodeDelay = preAverageNodeDelay;
	}

	/**
	 * 获取整段语音最后一个分片的请求回复延迟，如果当前回复不是最后分片的回复，则返回0
	 */
	public long getEndCutWriteDelay() {
		return this.isEndCut ? this.writeDelay : 0;
	}

	/**
	 * 获取整段语音最后一个分片的节点延迟，如果当前回复不是最后分片的回复，则返回0
	 */
	public long getEndCutNodeDelay() {
		return this.isEndCut ? this.nodeDelay : 0;
	}

	public long getMessageTime() {
		return messageTime;
	}

	public void setMessageTime(long messageTime) {
		this.messageTime = messageTime;
	}

	public long getNotifyDelay() {
		return notifyDelay;
	}

	public void setNotifyDelay() {
		this.notifyDelay = System.currentTimeMillis() - this.messageTime;
	}
}
