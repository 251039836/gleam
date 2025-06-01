package gleam.communication.inner.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.MessageDirectHandler;
import gleam.communication.MessageHandler;
import gleam.communication.Protocol;
import gleam.communication.inner.InnerClient;
import gleam.communication.inner.InnerClientConnectionListener;
import gleam.communication.inner.protocol.ReqInnerRpcForward;
import gleam.communication.inner.protocol.ResInnerReturnCode;
import gleam.communication.protocol.ProtocolUtil;
import gleam.core.service.Context;

/**
 * 内网转发rpc消息 接受端处理类<br>
 * 接受消息 拆包处理 并原路返回
 * 
 * @author hdh
 *
 */
public class ReqInnerRpcForwardDstHandler implements MessageDirectHandler<ReqInnerRpcForward> {
	private final static Logger logger = LoggerFactory.getLogger(ReqInnerRpcForwardDstHandler.class);

	private final static int DEFAULT_ERROR_CODE = 11001;
	private InnerClient client;

	private InnerClientConnectionListener<?> connectionListener;

	public ReqInnerRpcForwardDstHandler(InnerClient client, InnerClientConnectionListener<?> connectionListener) {
		this.client = client;
		this.connectionListener = connectionListener;
	}

	@Override
	public int getReqId() {
		return ReqInnerRpcForward.ID;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Protocol handleMessage(ReqInnerRpcForward protocol) {
		int srcServerType = protocol.getSrcServerType();
		int srcServerId = protocol.getSrcServerId();
		int dstServerType = protocol.getDstServerType();
		int dstServerId = protocol.getDstServerId();

		int forwardMsgId = protocol.getForwardMsgId();
		int forwardMsgSeq = protocol.getForwardMsgSeq();
		byte[] forwardMsgData = protocol.getForwardMsgData();
		Protocol forwardMessage = null;
		try {
			forwardMessage = ProtocolUtil.decodeMessage(forwardMsgId, forwardMsgSeq, forwardMsgData);
		} catch (Exception e) {
			logger.error("src[{}_{}] forward rpcMsg[{}] to dst[{}_{}] error.forwardMsg cant decode.", srcServerType,
					srcServerId, forwardMsgId, dstServerType, dstServerId, e);
			ResInnerReturnCode rpcResponse = new ResInnerReturnCode();
			rpcResponse.setSeq(forwardMsgSeq);
			rpcResponse.setCode(DEFAULT_ERROR_CODE);
			client.forwardMessage(srcServerType, srcServerId, rpcResponse);
			return null;
		}
		if (forwardMessage == null) {
			logger.error("src[{}_{}] forward rpcMsg[{}] to dst[{}_{}] error.forwardMsg not register.", srcServerType,
					srcServerId, forwardMsgId, dstServerType, dstServerId);
			ResInnerReturnCode rpcResponse = new ResInnerReturnCode();
			rpcResponse.setSeq(forwardMsgSeq);
			rpcResponse.setCode(DEFAULT_ERROR_CODE);
			client.forwardMessage(srcServerType, srcServerId, rpcResponse);
			return null;
		}
		MessageHandler messageHandler = connectionListener.getDirectHandlers().get(forwardMsgId);
		boolean flag = false;
		if (messageHandler != null) {
			flag = true;
			try {
				Protocol rpcResponse = messageHandler.handleMessage(forwardMessage);
				if (rpcResponse != null) {
					rpcResponse.setSeq(forwardMsgSeq);
					client.forwardMessage(srcServerType, srcServerId, rpcResponse);
					return null;
				}
			} catch (Exception e) {
				logger.error("src[{}_{}] forward rpcMsg[{}] to dst[{}_{}] error.forwardMsg[{}] handler error.",
						srcServerType, srcServerId, forwardMsgId, dstServerType, dstServerId,
						forwardMessage.getClass().getSimpleName());
				ResInnerReturnCode rpcResponse = new ResInnerReturnCode();
				rpcResponse.setSeq(forwardMsgSeq);
				rpcResponse.setCode(DEFAULT_ERROR_CODE);
				client.forwardMessage(srcServerType, srcServerId, rpcResponse);
				return null;
			}
		}
		if (!flag) {
			try {
				Context context = client.getOwner();
				if (context != null) {
					Protocol rpcResponse = context.handleMessage(forwardMessage);
					// 无对应处理类时抛错
					flag = true;
					if (rpcResponse != null) {
						rpcResponse.setSeq(-forwardMsgSeq);
						client.forwardMessage(srcServerType, srcServerId, rpcResponse);
						return null;
					}
				}
			} catch (Exception e) {
				logger.error("src[{}_{}] forward rpcMsg[{}] to dst[{}_{}] error.forwardMsg[{}] handler error.",
						srcServerType, srcServerId, forwardMsgId, dstServerType, dstServerId,
						forwardMessage.getClass().getSimpleName());
				ResInnerReturnCode rpcResponse = new ResInnerReturnCode();
				rpcResponse.setSeq(-forwardMsgSeq);
				rpcResponse.setCode(DEFAULT_ERROR_CODE);
				client.forwardMessage(srcServerType, srcServerId, rpcResponse);
				return null;
			}
		}
		ResInnerReturnCode rpcResponse = new ResInnerReturnCode();
		rpcResponse.setSeq(-forwardMsgSeq);
		client.forwardMessage(srcServerType, srcServerId, rpcResponse);
		return null;
	}

}
