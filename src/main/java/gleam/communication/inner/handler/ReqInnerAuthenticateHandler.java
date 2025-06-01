package gleam.communication.inner.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.authenticate.Authenticator;
import gleam.communication.authenticate.Identity;
import gleam.communication.authenticate.IdentityType;
import gleam.communication.define.DisconnectReason;
import gleam.communication.inner.InnerServer;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.communication.inner.protocol.ReqInnerAuthenticate;
import gleam.communication.inner.protocol.ResInnerAuthenticate;
import gleam.communication.server.ConnectionManager;

public class ReqInnerAuthenticateHandler
		implements MessageDirectHandler<ReqInnerAuthenticate>, Authenticator<ReqInnerAuthenticate> {

	protected final static Logger logger = LoggerFactory.getLogger(ReqInnerAuthenticateHandler.class);

	protected final InnerServer server;

	protected final ConnectionManager connectionManager;

	public ReqInnerAuthenticateHandler(InnerServer server) {
		super();
		this.server = server;
		this.connectionManager = server.getConnectionManager();
	}

	@Override
	public Identity authenticate(ReqInnerAuthenticate protocol) {
		if (!(protocol instanceof ReqInnerAuthenticate)) {
			logger.error("auth fail.protocol[{}] clazz[{}] is not ReqInnerAuthenticate.", protocol.getId(),
					protocol.getClass().getName());
			return null;
		}
		ReqInnerAuthenticate reqAuth = protocol;
		int serverId = reqAuth.getServerId();
		int serverType = reqAuth.getServerType();
		IdentityType identityType = IdentityType.get(serverType);
		if (identityType == null) {
			logger.error("auth fail.unknown identityType[{}]. serverId:{}.", serverType, serverId);
			return null;
		}
		if (!identityType.isInner()) {
			logger.error("auth fail.identityType[{}] not inner server. serverId:{}.", serverType, serverId);
			return null;
		}
		List<Integer> childIds = reqAuth.getChildIds();
		Identity identity = new InnerIdentity(serverId, childIds, identityType);
		return identity;
	}

	/**
	 * 返回服务端身份
	 * 
	 * @return
	 */
	protected ResInnerAuthenticate buildAuthResponse() {
		ResInnerAuthenticate authenticate = new ResInnerAuthenticate();
		authenticate.setServerId(server.getId());
		authenticate.setServerType(server.getType().getType());
		return authenticate;
	}

	@Override
	public int getReqId() {
		return ReqInnerAuthenticate.ID;
	}

	@Override
	public ResInnerAuthenticate handleMessage(ReqInnerAuthenticate message) {
		Identity identity = authenticate(message);
		if (identity == null) {
			return null;
		}
		Connection connection = message.getConnection();
		Connection oldConnection = connectionManager.bindingIdentity(identity, connection);
		if (oldConnection != null && !oldConnection.isClose()) {
			logger.error("identity[{}] repeat login.cur[{}] old[{}].close old connection.", identity.getKey(),
					connection.toFullName(), oldConnection.toFullName());
			oldConnection.close(DisconnectReason.SAME_IDENTITY);
		}
		logger.info("identity[{}] connection[{}] connected.", identity, connection.getRemoteIp());
		ResInnerAuthenticate response = buildAuthResponse();
		return response;
	}

	public InnerServer getServer() {
		return server;
	}

	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

}
