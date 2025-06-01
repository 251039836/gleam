package gleam.communication.inner.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.inner.InnerCommunicationService;
import gleam.communication.inner.InnerServer;
import gleam.communication.inner.protocol.ReqInnerForward;
import gleam.communication.protocol.EncodedProtocol;
import gleam.core.service.Context;

/**
 * 内网转发消息 转发节点处理类<br>
 * 拆包 并直接把实际发送的消息 发送到目标服务器
 * 
 * @author hdh
 *
 */
public class ReqInnerForwardHandler implements MessageDirectHandler<ReqInnerForward> {
	protected final static Logger logger = LoggerFactory.getLogger(ReqInnerForwardHandler.class);

	private final InnerServer server;

	public ReqInnerForwardHandler(InnerServer server) {
		this.server = server;
	}

	@Override
	public int getReqId() {
		return ReqInnerForward.ID;
	}

	@Override
	public Protocol handleMessage(ReqInnerForward protocol) {
		int srcServerType = protocol.getSrcServerType();
		int srcServerId = protocol.getSrcServerId();
		int dstServerType = protocol.getDstServerType();
		int dstServerId = protocol.getDstServerId();
		int forwardMsgId = protocol.getForwardMsgId();
		Connection dstConnection = null;
		if (server.getRemoteType().getType() == dstServerType) {
			dstConnection = server.getConnection(dstServerId);
		} else {
			// 目标服不是同个服务器管理 获取其他内网通信服务/多次转发
			Context context = server.getOwner();
			InnerCommunicationService communicationService = context.getCommunicationService(dstServerType);
			if (communicationService != null) {
				dstConnection = communicationService.getConnection(dstServerId);
			}
		}

		if (dstConnection == null) {
			logger.error(
					"srcServer[{}_{}] relayNode[{}_{}] forward msg[{}] dstServer[{}_{}] error.dstServer connection not exists.",
					srcServerType, srcServerId, server.getType().getType(), server.getId(), forwardMsgId, dstServerType,
					dstServerId);
			return null;
		}
		if (!dstConnection.isActive()) {
			logger.error(
					"srcServer[{}_{}] relayNode[{}_{}] forward msg[{}] dstServer[{}_{}] error.dstServer connection not actived.",
					srcServerType, srcServerId, server.getType().getType(), server.getId(), forwardMsgId, dstServerType,
					dstServerId);
			return null;
		}
		int forwardMsgSeq = protocol.getForwardMsgSeq();
		byte[] forwardMsgData = protocol.getForwardMsgData();
		EncodedProtocol forwardMsg = new EncodedProtocol(forwardMsgId, forwardMsgSeq, forwardMsgData);
		dstConnection.sendProtocol(forwardMsg);
		return null;
	}

	public InnerServer getServer() {
		return server;
	}

}
