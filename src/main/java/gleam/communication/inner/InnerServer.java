package gleam.communication.inner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import gleam.communication.Connection;
import gleam.communication.authenticate.Identity;
import gleam.communication.define.RpcConstant;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.communication.rpc.ConnectionRpcAddon;
import gleam.communication.server.impl.SocketServer;
import gleam.communication.task.CommunicationTaskManager;
import gleam.task.TaskHandle;

/**
 * 内网socket长连接服务端<br>
 * 用于服务器之间连接<br>
 * 支持向链接进来的服发起rpc调用
 *
 * @author hdh
 */
public abstract class InnerServer extends SocketServer implements InnerCommunicationService {

	/**
	 * 定时任务 检查并清理过期回调
	 */
	protected TaskHandle callbackTimerTask;

	@Override
	public void onStart() {
		super.onStart();
		startCallbackTimerTask();
	}

	@Override
	public void onStop() {
		super.onStop();
		cancelCallbackTimerTask();
	}

	private void cancelCallbackTimerTask() {
		if (callbackTimerTask != null) {
			callbackTimerTask.cancel();
		}
		callbackTimerTask = null;
	}

	/**
	 * 启动回调过期检查定时器
	 */
	private void startCallbackTimerTask() {
		cancelCallbackTimerTask();
		callbackTimerTask = CommunicationTaskManager.TIMER.scheduleTask(() -> {
			checkCallbackExpired();
		}, 0, RpcConstant.CALLBACK_EXPIRED_CHECK_INTERVAL);
	}

	protected void checkCallbackExpired() {
		ConcurrentMap<String, Connection> connectionMap = connectionManager.getConnectionMap();
		for (Connection connection : connectionMap.values()) {
			ConnectionRpcAddon rpcAddon = connection.getAttribute(ConnectionRpcAddon.ATTR_KEY);
			rpcAddon.checkExpired();
		}
	}

	/**
	 * 获取客户端连接
	 *
	 * @param serverId 服务器id(含子服)
	 * @return
	 */
	@Override
	public Connection getConnection(int serverId) {
		ConcurrentMap<Identity, Connection> identityConnectionMap = connectionManager.getIdentityConnectionMap();
		for (Entry<Identity, Connection> entry : identityConnectionMap.entrySet()) {
			Identity tmpIdentity = entry.getKey();
			if (!(tmpIdentity instanceof InnerIdentity)) {
				continue;
			}
			InnerIdentity tmpInnerIdentity = (InnerIdentity) tmpIdentity;
			if (tmpInnerIdentity.isInclude(serverId)) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public List<Connection> getAllIdentityConnections() {
		List<Connection> list = new ArrayList<>(connectionManager.getIdentityConnectionMap().values());
		return list;
	}

	public List<Connection> getConnections(Collection<Integer> serverIds) {
		if (serverIds == null || serverIds.isEmpty()) {
			return Collections.emptyList();
		}
		ConcurrentMap<Identity, Connection> identityConnectionMap = connectionManager.getIdentityConnectionMap();
		List<Connection> result = new ArrayList<>(serverIds.size());
		for (Entry<Identity, Connection> entry : identityConnectionMap.entrySet()) {
			Identity tmpIdentity = entry.getKey();
			if (!(tmpIdentity instanceof InnerIdentity)) {
				continue;
			}
			InnerIdentity tmpInnerIdentity = (InnerIdentity) tmpIdentity;
			for (int serverId : serverIds) {
				if (tmpInnerIdentity.isInclude(serverId)) {
					result.add(entry.getValue());
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 获取客户端连接（过滤掉自己服的链接并去重）
	 *
	 * @param ignoreServerId
	 * @param serverIds      服务器id列表(含子服)
	 * @return
	 */
	public List<Connection> getConnections(int ignoreServerId, List<Integer> serverIds) {
		ConcurrentMap<Identity, Connection> identityConnectionMap = connectionManager.getIdentityConnectionMap();
		List<Connection> result = new ArrayList<>(serverIds.size());
		for (Entry<Identity, Connection> entry : identityConnectionMap.entrySet()) {
			Identity tmpIdentity = entry.getKey();
			if (!(tmpIdentity instanceof InnerIdentity)) {
				continue;
			}
			InnerIdentity tmpInnerIdentity = (InnerIdentity) tmpIdentity;
			if (tmpInnerIdentity.getId() == ignoreServerId) {
				continue;
			}
			for (int serverId : serverIds) {
				if (tmpInnerIdentity.isInclude(serverId)) {
					result.add(entry.getValue());
					break;
				}
			}
		}
		return result;
	}

}
