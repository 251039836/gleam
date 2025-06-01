package gleam.communication.rpc.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import gleam.communication.Protocol;
import gleam.communication.rpc.RpcCallback;

public abstract class AbstractRpcCallback<T extends Protocol> implements RpcCallback<T> {

	protected int seq;

	protected long expiredTime;

	protected final AtomicBoolean complete = new AtomicBoolean(false);

	@Override
	public int getSeq() {
		return seq;
	}

	@Override
	public boolean isTimeout(long now) {
		return now > expiredTime;
	}

	public long getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(long expiredTime) {
		this.expiredTime = expiredTime;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

}
