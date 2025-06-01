package gleam.communication.inner.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.inner.InnerServer;
import gleam.communication.inner.protocol.ReqMultipleServerForward;
import gleam.communication.protocol.EncodedProtocol;

/**
 * 内网把消息转发给多个服务器 转发节点处理类<br>
 * 拆包 并直接把实际发送的消息 发送到目标服务器
 * 
 * @author lijr
 *
 */
public class ReqMultipleServerForwardHandler implements MessageDirectHandler<ReqMultipleServerForward> {
	protected final static Logger logger = LoggerFactory.getLogger(ReqMultipleServerForwardHandler.class);

	private final InnerServer server;

	public ReqMultipleServerForwardHandler(InnerServer server) {
		this.server = server;
	}

	@Override
	public int getReqId() {
		return ReqMultipleServerForward.ID;
	}

	@Override
	public Protocol handleMessage(ReqMultipleServerForward protocol) {
		int srcServerId = protocol.getSrcServerId();
		List<Integer> dstServerIds = protocol.getDstServerIds();
		int forwardMsgId = protocol.getForwardMsgId();
		byte[] forwardMsgData = protocol.getForwardMsgData();
		EncodedProtocol forwardMsg = new EncodedProtocol(forwardMsgId, forwardMsgData);
		List<Connection> connections = server.getConnections(srcServerId, dstServerIds);
		for (Connection c : connections) {
			c.sendProtocol(forwardMsg);
		}
		return null;
	}

	public InnerServer getServer() {
		return server;
	}

}
