package gleam.communication.inner;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import gleam.communication.Connection;
import gleam.communication.authenticate.IdentityType;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.core.define.ServiceStatus;
import gleam.core.service.AbstractService;
import gleam.exception.ServerStarupError;

/**
 * 连接指定类型服务器的客户端管理类
 * 
 * @author hdh
 *
 * @param <T>
 */
public abstract class InnerClientManager<T extends InnerClient> extends AbstractService
		implements InnerCommunicationService {

	protected final Map<Integer, T> clientMap = new HashMap<>();

	@Override
	protected void onInitialize() throws Exception {
		IdentityType remoteType = getRemoteType();
		Map<Integer, InetSocketAddress> clientConfigs = loadClientConfigs();
		if (clientConfigs == null || clientConfigs.isEmpty()) {
			logger.error("startup error.{}_Client config is empty.", remoteType.name());
			throw new ServerStarupError("startup error." + remoteType.name() + "_Client config is empty.");
		}
		InnerIdentity localIdentity = owner.getIdentity();
		for (Entry<Integer, InetSocketAddress> entry : clientConfigs.entrySet()) {
			int id = entry.getKey();
			InetSocketAddress remoteAddress = entry.getValue();
			InnerIdentity remoteIdentity = new InnerIdentity(id, remoteType);
			T client = newClient(localIdentity, remoteIdentity, remoteAddress);
			addClient(client);
		}
	}

	protected void addClient(T client) {
		client.setOwner(owner);
		client.initialize();
		clientMap.put(client.getRemoteIdentity().getId(), client);
		if (getStatus() == ServiceStatus.STARTED) {
			client.start();
		}
	}

	protected void removeClient(int serverId) {
		T client = clientMap.remove(serverId);
		if (client != null) {
			client.stop();
			client.destroy();
			client.setOwner(null);
		}
	}

	/**
	 * 加载客户端所需配置
	 * 
	 * @return <服务器id,服务器地址>
	 */
	protected abstract Map<Integer, InetSocketAddress> loadClientConfigs();

	/**
	 * 创建一个新的客户端
	 * 
	 * @param localIdentity
	 * @param remoteIdentity
	 * @param remoteAddress
	 * @return
	 */
	protected abstract T newClient(InnerIdentity localIdentity, InnerIdentity remoteIdentity,
			InetSocketAddress remoteAddress);

	@Override
	protected void onStart() {
		for (T client : clientMap.values()) {
			client.start();
		}
	}

	@Override
	protected void onStop() {
		for (T client : clientMap.values()) {
			client.stop();
		}
	}

	@Override
	protected void onDestroy() {
		for (T client : clientMap.values()) {
			client.destroy();
		}
	}

	public T getClient(int id) {
		return clientMap.get(id);
	}

	public Map<Integer, T> getClientMap() {
		return clientMap;
	}

	@Override
	public Connection getConnection(int serverId) {
		T client = clientMap.get(serverId);
		if (client != null) {
			return client.getConnection();
		}
		return null;
	}

	@Override
	public List<Connection> getAllIdentityConnections() {
		List<Connection> list = new ArrayList<>();
		for (T client : clientMap.values()) {
			if (client.isActive()) {
				list.add(client.getConnection());
			}
		}
		return list;
	}
}
