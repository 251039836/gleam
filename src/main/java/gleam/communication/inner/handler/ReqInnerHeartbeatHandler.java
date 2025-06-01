package gleam.communication.inner.handler;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.inner.protocol.ReqInnerHeartbeat;

public class ReqInnerHeartbeatHandler implements MessageDirectHandler<ReqInnerHeartbeat> {

	@Override
	public int getReqId() {
		return ReqInnerHeartbeat.ID;
	}

	@Override
	public Protocol handleMessage(ReqInnerHeartbeat message) {
		Connection connection = message.getConnection();
		long now = System.currentTimeMillis();
		connection.setHeartbeatTime(now);
		return null;
	}

}
