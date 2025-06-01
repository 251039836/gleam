package gleam.communication.message;

import gleam.util.time.TimeUtil;

/**
 * 可以重试的消息
 * 
 * @author redback
 * @version 1.00
 * @time 2023-7-5 16:35
 */
public class RetryableInfo {

	/**
	 * 默认最大重试次数
	 */
	public static final int DEFAULT_MAX_RETRY_NUM = 10;

	/**
	 * 消息唯一 id
	 */
	private long uid;

	/**
	 * 当前重试次数
	 */
	private int retryNum;

	/**
	 * 最大可重试次数(小于 0， 代表着无限次重试，慎重使用)
	 */
	private int maxRetryNum = DEFAULT_MAX_RETRY_NUM;

	/**
	 * 上次重试时间
	 */
	private long lastRetryTime;

	public static RetryableInfo build(long uid) {
		RetryableInfo retryableInfo = new RetryableInfo();
		retryableInfo.setUid(uid);
		return retryableInfo;
	}

	/**
	 * 消息是否过期，到达最大重试次数
	 * 
	 * @return true/false
	 */
	public boolean isExpire() {
		return maxRetryNum >= 0 && retryNum >= maxRetryNum;
	}

	public void updateRetryInfo(long time) {
		this.lastRetryTime = time;
		this.retryNum += 1;
	}

	/**
	 * 获取下次重试时间间隔
	 * 
	 * @param curRetryNum 当前重试时间
	 * @return ms
	 */
	public long getNextRetryTime(int curRetryNum) {
		if (curRetryNum <= 3) {// 三次之内，快速重试
			return 5 * TimeUtil.SECOND_MILLISECONDS;
		} else if (curRetryNum <= 5) {
			return 20 * TimeUtil.SECOND_MILLISECONDS;
		} else if (curRetryNum <= 10) {
			return 30 * TimeUtil.SECOND_MILLISECONDS;
		} else {
			return TimeUtil.MINUTE_MILLISECONDS;
		}
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public int getRetryNum() {
		return retryNum;
	}

	public void setRetryNum(int retryNum) {
		this.retryNum = retryNum;
	}

	public int getMaxRetryNum() {
		return maxRetryNum;
	}

	public void setMaxRetryNum(int maxRetryNum) {
		this.maxRetryNum = maxRetryNum;
	}

	public long getLastRetryTime() {
		return lastRetryTime;
	}

	public void setLastRetryTime(long lastRetryTime) {
		this.lastRetryTime = lastRetryTime;
	}
}
