package gleam.core.ref;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.authenticate.IdentityType;
import gleam.communication.define.RpcConstant;
import gleam.communication.inner.InnerCommunicationService;
import gleam.communication.inner.protocol.ResInnerReturnCode;
import gleam.communication.protocol.ProtocolUtil;
import gleam.communication.rpc.RpcCallbackCache;
import gleam.core.Entity;
import gleam.core.define.BasicErrorCode;
import gleam.core.ref.callback.EntityFowardAskCallback;
import gleam.core.ref.define.EntityRefState;
import gleam.core.ref.define.EntityType;
import gleam.core.ref.impl.RemoteEntityRef;
import gleam.core.ref.impl.SimpleEntityAddressFinder;
import gleam.core.ref.impl.SimpleLocalEntityFinder;
import gleam.core.ref.protocol.ReqEntityCheck;
import gleam.core.ref.protocol.ReqEntityForward;
import gleam.core.ref.protocol.ResEntityCheck;
import gleam.core.ref.protocol.ResEntityForward;
import gleam.core.service.AbstractService;
import gleam.task.TaskHandle;
import gleam.task.TaskManager;
import gleam.util.ClazzUtil;
import gleam.util.time.TimeUtil;

/**
 * 引用管理类<br>
 * 管理当前进程引用的异地进程 和查找当前进程的实体
 * 
 * @author hdh
 *
 */
public class EntityRefManager extends AbstractService {

	private final static EntityRefManager instance = new EntityRefManager();

	public static EntityRefManager getInstance() {
		return instance;
	}

	public final static long TICK_INTERVAL = TimeUnit.SECONDS.toMillis(10);

	/**
	 * 类型,id,引用
	 */
	private final ConcurrentMap<Integer, ConcurrentMap<Long, RemoteEntityRef>> entityRefs = new ConcurrentHashMap<>();

	/**
	 * 定时任务 检查并清理失效/不活跃引用
	 */
	private TaskHandle tickTask;
	/**
	 * 定时任务 检查并清理过期回调
	 */
	private TaskHandle callbackTimerTask;

	private EntityAddressFinder addressFinder;

	private LocalEntityFinder localEntityFinder;

	private TaskManager taskManager;

	@Override
	protected void onInitialize() throws Exception {
		taskManager = TaskManager.buildSmallInstance("entityRef");
		List<EntityAddressFinder> tmpAddressFinders = ClazzUtil.scanImplAndNewInstances(ClazzUtil.GAME_PACKAGE_NAME,
				EntityAddressFinder.class);
		if (tmpAddressFinders != null) {
			setAddressFinder(tmpAddressFinders.get(0));
		} else {
			setAddressFinder(new SimpleEntityAddressFinder());
		}

		List<LocalEntityFinder> tmpLocalEntityFinders = ClazzUtil.scanImplAndNewInstances(ClazzUtil.GAME_PACKAGE_NAME,
				LocalEntityFinder.class);
		if (tmpLocalEntityFinders != null) {
			localEntityFinder = tmpLocalEntityFinders.get(0);
		} else {
			localEntityFinder = new SimpleLocalEntityFinder(owner);
		}

	}

	@Override
	protected void onStart() throws Exception {
		startTickTask();
		startCallbackTimerTask();
		registerMessageHandler(ReqEntityForward.ID);
		registerMessageHandler(ReqEntityCheck.ID);
		registerMessageHandler(ResEntityForward.ID);
	}

	@Override
	protected void onStop() {
		removeMessageHandler(ReqEntityForward.ID);
		removeMessageHandler(ReqEntityCheck.ID);
		removeMessageHandler(ResEntityForward.ID);
		cancelTickTask();
		cancelCallbackTimerTask();
	}

	private void cancelTickTask() {
		if (tickTask != null) {
			tickTask.cancel();
		}
		tickTask = null;
	}

	private void cancelCallbackTimerTask() {
		if (callbackTimerTask != null) {
			callbackTimerTask.cancel();
		}
		callbackTimerTask = null;
	}

	private void startTickTask() {
		cancelTickTask();
		tickTask = taskManager.scheduleTaskStartAtNextMinute(() -> {
			tick();
		}, TICK_INTERVAL);
	}

	/**
	 * 启动回调过期检查定时器
	 */
	private void startCallbackTimerTask() {
		cancelCallbackTimerTask();
		callbackTimerTask = taskManager.scheduleTask(() -> {
			checkCallbackExpired();
		}, 0, RpcConstant.CALLBACK_EXPIRED_CHECK_INTERVAL);
	}

	private void checkCallbackExpired() {
		for (Entry<Integer, ConcurrentMap<Long, RemoteEntityRef>> entry : entityRefs.entrySet()) {
			ConcurrentMap<Long, RemoteEntityRef> typeRefs = entry.getValue();
			if (typeRefs.isEmpty()) {
				continue;
			}
			long now = TimeUtil.now();
			for (Entry<Long, RemoteEntityRef> entry2 : typeRefs.entrySet()) {
				RemoteEntityRef entityRef = entry2.getValue();
				RpcCallbackCache callbackCache = entityRef.getCallbackCache();
				callbackCache.checkExpired(now);
			}
		}
	}

	private void tick() {
		// 清理过期/长时间未使用对象
		clearExpiredRefs();
	}

	/**
	 * 清理过期/长时间未使用对象
	 */
	private void clearExpiredRefs() {
		if (entityRefs.isEmpty()) {
			return;
		}
		for (Entry<Integer, ConcurrentMap<Long, RemoteEntityRef>> entry : entityRefs.entrySet()) {
			ConcurrentMap<Long, RemoteEntityRef> typeRefs = entry.getValue();
			if (typeRefs.isEmpty()) {
				continue;
			}
			int type = entry.getKey();
			EntityType entityType = EntityType.valueOf(type);
			if (entityType == null) {
				logger.error("unknown entityType[{}]", type);
				continue;
			}
			long now = TimeUtil.now();
			long invalidTimeLimit = now - entityType.getInvalidTime();
			long inactiveTimeLimit = -1;
			long inactiveTime = entityType.getInactiveTime();
			if (inactiveTime > 0) {
				inactiveTimeLimit = now - inactiveTime;
			}
			for (Entry<Long, RemoteEntityRef> entry2 : typeRefs.entrySet()) {
				long id = entry2.getKey();
				RemoteEntityRef entityRef = entry2.getValue();
				int state = entityRef.getState().get();
				if (state == EntityRefState.INVALID && now >= invalidTimeLimit) {
					typeRefs.remove(id);
				} else if (state == EntityRefState.CHECK
						&& now >= entityRef.getCreateTime() + RpcConstant.DEFAULT_RPC_TIMEOUT * 2) {
					// 判断实体是否存在超时 视为实体失效
					if (entityRef.getState().compareAndSet(EntityRefState.CHECK, EntityRefState.INVALID)) {
						entityRef.setLastTime(now);
					}
				} else if (inactiveTimeLimit > 0 && inactiveTimeLimit > entityRef.getLastTime()) {
					typeRefs.remove(id);
				}
			}
		}
	}

	public EntityRef getRef(int type, long id) {
		// 判断是否当前进程实体
		if (localEntityFinder.isLocalEntity(type, id)) {
			return localEntityFinder.getLocalRef(type, id);
		}
		// 不在当前进程
		return getRemoteRef(type, id, true);
	}

	private RemoteEntityRef getRemoteRef(int entityType, long entityId, boolean createIfAbsent) {
		ConcurrentMap<Long, RemoteEntityRef> typeRefs = entityRefs.get(entityType);
		if (typeRefs == null && createIfAbsent) {
			typeRefs = new ConcurrentHashMap<>();
			ConcurrentMap<Long, RemoteEntityRef> oldData = entityRefs.putIfAbsent(entityType, typeRefs);
			if (oldData != null) {
				typeRefs = oldData;
			}
		}
		RemoteEntityRef entityRef = typeRefs.get(entityId);
		if (entityRef == null && createIfAbsent) {
			entityRef = buildRemoteRef(entityType, entityId);
			RemoteEntityRef oldRef = typeRefs.putIfAbsent(entityId, entityRef);
			if (oldRef != null) {
				entityRef = oldRef;
			}
		}
		if (entityRef.getState().get() == EntityRefState.INIT) {
			checkEntityExist(entityRef);
		}
		return entityRef;
	}

	/**
	 * 判断实体是否存在
	 * 
	 * @param entityRef
	 */
	private void checkEntityExist(RemoteEntityRef entityRef) {
		AtomicInteger state = entityRef.getState();
		if (!state.compareAndSet(EntityRefState.INIT, EntityRefState.CHECK)) {
			return;
		}
		int entityType = entityRef.getEntityType();
		long entityId = entityRef.getId();
		EntityAddress address = entityRef.getAddress();
		int dstServetType = address.getType().getType();
		int dstServerId = address.getId();
		Connection connection = getServerConnection(dstServetType, dstServerId);
		if (connection == null || !connection.isActive()) {
			// 链接失效 无法判断是否存在 视为不存在
			state.set(EntityRefState.INVALID);
			return;
		}
		ReqEntityCheck request = new ReqEntityCheck();
		request.setDstServerType(dstServetType);
		request.setDstServerId(dstServerId);
		request.setDstEntityType(entityType);
		request.setDstEntityId(entityId);
		request.setSrcServerType(owner.getServerType().getType());
		request.setSrcServerId(owner.getServerId());
		connection.sendProtocol(request);
		// 直接在tick时做过期检测
	}

	private RemoteEntityRef buildRemoteRef(int type, long id) {
		long now = TimeUtil.now();
		RemoteEntityRef ref = new RemoteEntityRef();
		ref.setType(type);
		ref.setState(EntityRefState.INIT);
		ref.setCreateTime(now);
		ref.setLastTime(now);
		EntityAddress address = addressFinder.findAddress(type, id);
		if (address != null) {
			ref.setAddress(address);
		} else {
			ref.setState(EntityRefState.INVALID);
		}
		return ref;
	}

	public TaskHandle getTickTask() {
		return tickTask;
	}

	public void setTickTask(TaskHandle tickTask) {
		this.tickTask = tickTask;
	}

	@Override
	public Protocol handleMessage(Protocol message) {
		int id = message.getId();
		if (id == ReqEntityForward.ID) {
			return reqEntityForward((ReqEntityForward) message);
		} else if (id == ReqEntityCheck.ID) {
			return reqEntityCheck((ReqEntityCheck) message);
		} else if (id == ResEntityForward.ID) {
			return resEntityForward((ResEntityForward) message);
		} else if (id == ResEntityCheck.ID) {
			return resEntityCheck((ResEntityCheck) message);
		}
		return null;
	}

	private Protocol resEntityCheck(ResEntityCheck message) {
		int srcServerType = message.getSrcServerType();
		int srcServerId = message.getSrcServerId();
		if (!isThisServer(srcServerType, srcServerId)) {
			// 继续转发
			sendMessage2DstServer(srcServerType, srcServerId, message);
			return null;
		}
		int dstEntityType = message.getDstEntityType();
		long dstEntityId = message.getDstEntityId();
		boolean dstEntityExists = message.isDstEntityExists();
		RemoteEntityRef remoteRef = getRemoteRef(dstEntityType, dstEntityId, true);
		// 设置状态 清理当前累积的任务
		remoteRef.handleCheckResult(dstEntityExists);
		return null;
	}

	private Protocol reqEntityCheck(ReqEntityCheck request) {
		int dstServerType = request.getDstServerType();
		int dstServerId = request.getDstServerId();
		Connection connection = request.getConnection();
		if (!isThisServer(dstServerType, dstServerId)) {
			// 继续转发
			boolean success = sendMessage2DstServer(dstServerType, dstServerId, request);
			if (!success) {
				// 找不到目标服务器 返回来源服务器
				ResEntityCheck response = buildCheckResponse(request, false);
				connection.sendProtocol(response);
			}
			return null;
		}
		// 当前服
		int dstEntityType = request.getDstEntityType();
		long dstEntityId = request.getDstEntityId();
		Entity<?> entity = localEntityFinder.getLocalEntity(dstEntityType, dstEntityId);
		ResEntityCheck response = buildCheckResponse(request, entity != null);
		connection.sendProtocol(response);
		return null;
	}

	private Protocol resEntityForward(ResEntityForward request) {
//		int dstServerId = request.getDstServerId();
//		int dstServerType = request.getDstServerType();
		int dstEntityType = request.getDstEntityType();
		long dstEntityId = request.getDstEntityId();
		RemoteEntityRef entityRef = getRemoteRef(dstEntityType, dstEntityId, false);
		if (entityRef == null) {
			logger.error("resEntityForward entity[{}_{}] not exists.", dstEntityType, dstEntityId);
			return null;
		}
		int forwardMsgId = request.getForwardMsgId();
		int forwardMsgSeq = request.getForwardMsgSeq();
		byte[] forwardMsgData = request.getForwardMsgData();
		Protocol forwardMsg = null;
		try {
			forwardMsg = ProtocolUtil.decodeMessage(forwardMsgId, forwardMsgSeq, forwardMsgData);
		} catch (Exception e) {
			logger.error("resEntityForward decode msg[{}] fail.entity[{}_{}]", forwardMsgId, dstEntityType, dstEntityId,
					e);
			return null;
		}
		boolean handle = entityRef.getCallbackCache().receiveResponse(-forwardMsgSeq, forwardMsg);
		if (!handle) {
			// rpc超时 扔到context处理
			owner.handleMessage(forwardMsg);
		}
		return null;
	}

	/**
	 * 请求转发消息到目标实体
	 * 
	 * @param request
	 * @return
	 */
	private Protocol reqEntityForward(ReqEntityForward request) {
		int dstServerType = request.getDstServerType();
		int dstServerId = request.getDstServerId();
		int dstEntityType = request.getDstEntityType();
		long dstEntityId = request.getDstEntityId();
		int forwardMsgSeq = request.getForwardMsgSeq();
		if (!isThisServer(dstServerType, dstServerId)) {
			// 继续转发
			boolean success = sendMessage2DstServer(dstServerType, dstServerId, request);
			if (!success && forwardMsgSeq > 0) {
				// 找不到目标服务器 返回来源服务器
				Connection connection = request.getConnection();
				ResEntityForward response = buildFowardErrorResponse(request, BasicErrorCode.DST_SERVER_CANT_REACH);
				connection.sendProtocol(response);
			}
			return null;
		}
		// 本服
		EntityRef entityRef = localEntityFinder.getLocalRef(dstEntityType, dstEntityId);
		if (entityRef == null && forwardMsgSeq > 0) {
			// 无对应实体管理类
			Connection connection = request.getConnection();
			ResEntityForward response = buildFowardErrorResponse(request, BasicErrorCode.ENTITY_NOT_EXISTS);
			connection.sendProtocol(response);
			return null;
		}
		int forwardMsgId = request.getForwardMsgId();
		byte[] forwardMsgData = request.getForwardMsgData();
		Protocol fowardMsg = null;
		try {
			fowardMsg = ProtocolUtil.decodeMessage(forwardMsgId, forwardMsgSeq, forwardMsgData);
		} catch (Exception e) {
			logger.error("reqEntityForward decode msg[{}] fail.entity[{}_{}]", forwardMsgId, dstEntityType, dstEntityId,
					e);
		}
		if (fowardMsg == null) {
			Connection connection = request.getConnection();
			ResEntityForward response = buildFowardErrorResponse(request, BasicErrorCode.PROTOCOL_CANT_DECODE);
			connection.sendProtocol(response);
			return null;
		}
		// 指定实体处理实际的消息
		if (forwardMsgSeq == 0) {
			entityRef.tell(fowardMsg);
		} else {
			EntityFowardAskCallback callback = new EntityFowardAskCallback(request);
			entityRef.ask(fowardMsg, RpcConstant.DEFAULT_RPC_TIMEOUT, callback);
		}
		return null;
	}

	private ResEntityCheck buildCheckResponse(ReqEntityCheck request, boolean exists) {
		ResEntityCheck response = new ResEntityCheck();
		response.setSrcServerType(request.getSrcServerType());
		response.setSrcServerId(request.getSrcServerId());
		response.setDstServerType(request.getDstServerType());
		response.setDstServerId(request.getDstServerId());
		response.setDstEntityType(request.getDstEntityType());
		response.setDstEntityId(request.getDstEntityId());
		response.setDstEntityExists(exists);
		return response;
	}

	private ResEntityForward buildFowardErrorResponse(ReqEntityForward request, int errorCode) {
		ResEntityForward response = new ResEntityForward();
		response.setSrcServerType(request.getSrcServerType());
		response.setSrcServerId(request.getSrcServerId());
		response.setDstServerType(request.getDstServerType());
		response.setDstServerId(request.getDstServerId());
		response.setDstEntityType(request.getDstEntityType());
		response.setDstEntityId(request.getDstEntityId());
		ResInnerReturnCode rc = new ResInnerReturnCode();
		rc.setCode(errorCode);
		byte[] forwardMsgData = ProtocolUtil.encodeMessage(rc);
		response.setForwardMsgSeq(-request.getForwardMsgSeq());
		response.setForwardMsgId(rc.getId());
		response.setForwardMsgData(forwardMsgData);
		return response;
	}

	private boolean sendMessage2DstServer(int dstServerType, int dstServerId, Protocol protocol) {
		Connection connection = getServerConnection(dstServerType, dstServerId);
		if (connection == null) {
			return false;
		}
		connection.sendProtocol(protocol);
		return true;
	}

	public Connection getServerConnection(int serverType, int serverId) {
		// 优先查找直接链接的服
		InnerCommunicationService communicationService = owner.getCommunicationService(serverType);
		if (communicationService != null) {
			Connection connection = communicationService.getConnection(serverId);
			return connection;
		}
		// 查找可以转发的服
		// FIXME 暂不弄个查找路径的逻辑 直接找常规的转发节点
		communicationService = owner.getCommunicationService(IdentityType.CROSS.getType());
		List<Connection> allIdentityConnections = communicationService.getAllIdentityConnections();
		if (allIdentityConnections == null || allIdentityConnections.isEmpty()) {
			return null;
		}
		if (allIdentityConnections.size() == 1) {
			return allIdentityConnections.get(0);
		}
		int index = serverId % allIdentityConnections.size();
		return allIdentityConnections.get(index);
	}

	private boolean isThisServer(int serverType, int serverId) {
		if (owner.getServerId() != serverId) {
			return false;
		}
		if (owner.getServerType().getType() != serverType) {
			return false;
		}
		return true;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public EntityAddressFinder getAddressFinder() {
		return addressFinder;
	}

	public void setAddressFinder(EntityAddressFinder addressFinder) {
		this.addressFinder = addressFinder;
	}

}
