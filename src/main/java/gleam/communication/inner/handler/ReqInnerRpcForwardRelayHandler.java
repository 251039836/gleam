package gleam.communication.inner.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.inner.InnerServer;
import gleam.communication.inner.protocol.ReqInnerRpcForward;
import gleam.communication.inner.protocol.ResInnerReturnCode;

/**
 * 内网转发rpc消息 转发节点处理类<br>
 * 直接带包装进行转发
 * 
 * @author hdh
 *
 */
public class ReqInnerRpcForwardRelayHandler implements MessageDirectHandler<ReqInnerRpcForward> {
	protected final static Logger logger = LoggerFactory.getLogger(ReqInnerRpcForwardRelayHandler.class);

	private final static int DEFAULT_ERROR_CODE = 11001;
	private final InnerServer server;

	public ReqInnerRpcForwardRelayHandler(InnerServer server) {
		this.server = server;
	}

	@Override
	public int getReqId() {
		return ReqInnerRpcForward.ID;
	}

	@Override
	public Protocol handleMessage(ReqInnerRpcForward protocol) {
		int srcServerId = protocol.getSrcServerId();
		int dstServerId = protocol.getDstServerId();
		int forwardMsgId = protocol.getForwardMsgId();
		Connection dstConnection = server.getConnection(dstServerId);
		if (dstConnection == null) {
			logger.error(
					"srcServer[{}] relayNode[{}] forward rpcMsg[{}] dstServer[{}] error.dstServer connection not exists.",
					srcServerId, server.getId(), forwardMsgId, dstServerId);
			ResInnerReturnCode response = new ResInnerReturnCode();
			response.setCode(DEFAULT_ERROR_CODE);
			response.setSeq(-protocol.getForwardMsgSeq());
			return response;
		}
		if (!dstConnection.isActive()) {
			logger.error(
					"srcServer[{}] relayNode[{}] forward rpcMsg[{}] dstServer[{}] error.dstServer connection not actived.",
					srcServerId, server.getId(), forwardMsgId, dstServerId);
			ResInnerReturnCode response = new ResInnerReturnCode();
			response.setCode(DEFAULT_ERROR_CODE);
			response.setSeq(-protocol.getForwardMsgSeq());
			return response;
		}
		dstConnection.sendProtocol(protocol);
		return null;
	}

	public InnerServer getServer() {
		return server;
	}

}
