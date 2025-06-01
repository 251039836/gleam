package gleam.communication.inner.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.client.Client;
import gleam.communication.inner.InnerClient;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.communication.inner.protocol.ResInnerAuthenticate;

public class ResInnerAuthenticateHandler implements MessageDirectHandler<ResInnerAuthenticate> {

	protected final static Logger logger = LoggerFactory.getLogger(ResInnerAuthenticateHandler.class);

	protected final InnerClient client;

	public ResInnerAuthenticateHandler(InnerClient client) {
		super();
		this.client = client;
	}

	public Client getClient() {
		return client;
	}

	@Override
	public int getReqId() {
		return ResInnerAuthenticate.ID;
	}

	@Override
	public Protocol handleMessage(ResInnerAuthenticate message) {
		Connection connection = message.getConnection();
		int serverId = message.getServerId();
		int serverType = message.getServerType();
		InnerIdentity remoteIdentity = client.getRemoteIdentity();
		if (serverId == remoteIdentity.getId() || serverType == remoteIdentity.getType().getType()) {
			connection.setIdentity(remoteIdentity);
			logger.error("client[{}] connect server[{}] success.resAuth serverId={},serverType={}", client.getName(),
					remoteIdentity.getKey(), serverId, serverType);
			return null;
		}
		logger.error("client[{}] connect server[{}] error.resAuth serverId={},serverType={}", client.getName(),
				remoteIdentity.getKey(), serverId, serverType);
		return null;
	}

}
