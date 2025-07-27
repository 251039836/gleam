package gleam.core.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import gleam.communication.MessageHandler;
import gleam.communication.Protocol;
import gleam.communication.authenticate.IdentityType;
import gleam.communication.inner.InnerCommunicationService;
import gleam.communication.inner.auth.InnerIdentity;
import gleam.config.ServerSettings;
import gleam.core.AbstractEntity;
import gleam.core.define.ServiceStatus;
import gleam.core.event.GameEvent;
import gleam.core.executor.task.EntityHandleEventTask;
import gleam.core.ref.EntityManager;
import gleam.exception.ServerStarupError;
import gleam.task.Task;
import gleam.task.TaskManager;
import gleam.task.token.DefaultFutureTask;
import gleam.util.ClazzUtil;

/**
 * 进程上下文<br>
 * 默认每个进程对应1个上下文<br>
 * 挂载所有需要用到的公共服务类<br>
 * 
 * @author hdh
 *
 */
public abstract class Context extends AbstractEntity<Service> implements Zone<Service> {
	/**
	 * 服务器id
	 */
	protected int id;

	protected InnerIdentity identity;

	protected volatile ServiceStatus status = ServiceStatus.ORIGINAL;
	/**
	 * 远端服务器类型,通信服务类
	 */
	protected Map<Integer, InnerCommunicationService> communicationServices = new HashMap<>();
	/**
	 * 实体类型,实体管理类
	 */
	protected Map<Integer, EntityManager<?>> entityManagers = new HashMap<>();

	@Override
	public long getId() {
		return id;
	}

	/**
	 * 当前服务器类型
	 * 
	 * @return
	 */
	public abstract IdentityType getServerType();

	/**
	 * 服务器id
	 * 
	 * @return
	 */
	public int getServerId() {
		return identity.getId();
	}

	public InnerIdentity getIdentity() {
		return identity;
	}

	/**
	 * 服务器id在配置文件中的key
	 * 
	 * @return
	 */
	protected abstract String getServerIdKey();

	protected List<Service> getSortList(boolean reverse) {
		List<Service> list = new ArrayList<>();
		list.addAll(components.values());
		Collections.sort(list);
		if (reverse) {
			Collections.reverse(list);
		}
		return list;
	}

	@Override
	public int getPriority() {
		return PRIORITY_HIGHEST;
	}

	@Override
	public int compareTo(Service o) {
		return Integer.compare(o.getPriority(), getPriority());
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public ServiceStatus getStatus() {
		return status;
	}

	@Override
	public void initialize() {
		long beginTime = System.currentTimeMillis();
		status = ServiceStatus.INITIALIZING;
		logger.info("{} initialize begin.", getName());
		try {
			loadSettings();
		} catch (Throwable e) {
			logger.error("load settings error.", e);
			throw new ServerStarupError(e);
		}
		registerAll();
		// 初始化
		List<Service> list = getSortList(false);
		for (Service service : list) {
			long serviceBeginTime = System.currentTimeMillis();
			try {
				service.initialize();
			} catch (Throwable e) {
				logger.error("service[{}] init error.", service.getName(), e);
				throw e;
			}
			long serviceCostTime = System.currentTimeMillis() - serviceBeginTime;
			if (serviceCostTime > 500) {
				logger.info("service[{}] initialize costTime={}ms", service.getName(), serviceCostTime);
			}
		}
		// 监听关闭进程
		// 初始化之后才开始监听
		registerShutdownHook();
		status = ServiceStatus.INITIALIZED;
		long costTime = System.currentTimeMillis() - beginTime;
		logger.info("{} initialize success.costTime={}ms", getName(), costTime);
	}

	@Override
	public void start() {
		long beginTime = System.currentTimeMillis();
		status = ServiceStatus.STARTING;
		logger.info("services start begin.");
		List<Service> list = getSortList(false);
		for (Service service : list) {
			long serviceBeginTime = System.currentTimeMillis();
			try {
				service.start();
			} catch (Throwable e) {
				logger.error("service[{}] ready error.", service.getName(), e);
				throw e;
			}
			long serviceCostTime = System.currentTimeMillis() - serviceBeginTime;
			if (serviceCostTime > 500) {
				logger.info("service[{}] start costTime={}ms", service.getName(), serviceCostTime);
			}
		}
		status = ServiceStatus.STARTED;
		long costTime = System.currentTimeMillis() - beginTime;
		logger.info("{} start success.costTime={}ms", getName(), costTime);
	}

	@Override
	public void stop() {
		status = ServiceStatus.STOPPING;
		logger.info("{} stop begin.", getName());
		List<Service> list = getSortList(true);
		for (Service service : list) {
			try {
				service.stop();
			} catch (Throwable e) {
				logger.error("service[{}] stop error.", service.getName(), e);
			}
		}
		status = ServiceStatus.STOPPED;
		logger.info("{} stop success.", getName());
	}

	@Override
	public void destroy() {
		status = ServiceStatus.DESTROYING;
		logger.info("{} destroy begin.", getName());
		List<Service> list = getSortList(true);
		for (Service service : list) {
			try {
				service.destroy();
			} catch (Throwable e) {
				logger.error("service[{}] destroy error.", service.getName(), e);
			}
		}
		status = ServiceStatus.DESTROYED;
		logger.info("{} destroy success.", getName());
	}

	public void shutdown() {
		logger.info("{} shutdown begin.", getName());
		stop();
		destroy();
		logger.info("{} shutdown success.", getName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Protocol handleMessage(Protocol message) {
		if (message == null) {
			throw new NullPointerException("handleProtocol error.msg is null.");
		}
		int msgId = message.getId();
		MessageHandler handler = messageHandlers.get(msgId);
		if (handler == null) {
			logger.warn("receive message[{}],but handler is null.", msgId);
			throw new NullPointerException("handleProtocol error.msgHandler is null.");
		}
		long beginTime = System.currentTimeMillis();
		Protocol response = handler.handleMessage(message);
		long costTime = System.currentTimeMillis() - beginTime;
		if (costTime > 100) {
			logger.warn("{}[{}] handle message[{}],costTime={}ms", getClass().getSimpleName(), getId(), msgId,
					costTime);
		}
		return response;
	}

	/**
	 * 加载服务端身份
	 */
	protected void loadServerIdentity() {
		String serverIdKey = getServerIdKey();
		int serverId = ServerSettings.getIntProperty(serverIdKey);
		IdentityType serverType = getServerType();

		identity = new InnerIdentity(serverId, serverType);
	}

	/**
	 * 加载服务器设置
	 */
	protected void loadSettings() {
		loadServerIdentity();
	}

	/**
	 * 注册监听进程关闭钩子
	 */
	protected void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			shutdown();
		}));
	}

	/**
	 * 注册所有服务
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void registerAll() {
		try {
			List<ContextRegister<?>> registerList = scanRegisterList();
			for (ContextRegister register : registerList) {
				register.registerAll(this);
			}
		} catch (Throwable e) {
			logger.error("context auto register error.", e);
			throw new ServerStarupError(e);
		}
	}

	public void registerService(Service service) {
		String name = service.getName();
		registerComponent(name, service);
		if (service instanceof InnerCommunicationService ics) {
			communicationServices.put(ics.getRemoteType().getType(), ics);
		}
		if (service instanceof EntityManager em) {
			entityManagers.put(em.getEntityType(), em);
		}
	}

	public void removeService(Service service) {
		String name = service.getName();
		removeComponent(name);
		if (service instanceof InnerCommunicationService ics) {
			communicationServices.remove(ics.getRemoteType().getType(), ics);
		}
		if (service instanceof EntityManager em) {
			entityManagers.remove(em.getEntityType(), em);
		}
	}

	/**
	 * 扫描register包下的注册类
	 * 
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	protected List<ContextRegister<?>> scanRegisterList() throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		List<ContextRegister<?>> registerList = new ArrayList<>();
		String registerPackage = getRegisterPackage();
		List<Class<?>> registerClazzList = ClazzUtil.scanClassList(registerPackage, (clazz) -> {
			if (!ContextRegister.class.isAssignableFrom(clazz)) {
				return false;
			}
			Class<?>[] parameterizedTypeClazzes = ClazzUtil.getParameterizedTypeClazzes(clazz, ContextRegister.class);
			if (parameterizedTypeClazzes == null || parameterizedTypeClazzes.length <= 0) {
				return false;
			}
			if (parameterizedTypeClazzes.getClass().equals(getClass())) {
				return false;
			}
			return true;
		});
		for (Class<?> registerClazz : registerClazzList) {
			ContextRegister<?> register = (ContextRegister<?>) registerClazz.getConstructor().newInstance();
			registerList.add(register);
		}
		return registerList;
	}

	/**
	 * 获取注册类目录<br>
	 * 
	 * @return
	 */
	protected String getRegisterPackage() {
		String contextPackage = getClass().getPackage().getName();
		String registerPackage = contextPackage + ".register";
		return registerPackage;
	}

	public Map<Integer, InnerCommunicationService> getCommunicationServices() {
		return communicationServices;
	}

	public void setCommunicationServices(Map<Integer, InnerCommunicationService> communicationServices) {
		this.communicationServices = communicationServices;
	}

	public Map<Integer, EntityManager<?>> getEntityManagers() {
		return entityManagers;
	}

	public void setEntityManagers(Map<Integer, EntityManager<?>> entityManagers) {
		this.entityManagers = entityManagers;
	}

	public EntityManager<?> getEntityManager(int entityType) {
		return entityManagers.get(entityType);
	}

	public InnerCommunicationService getCommunicationService(int serverType) {
		return communicationServices.get(serverType);
	}

	@Override
	public void submitTask(Task task) {
		TaskManager.getInstance().scheduleTask(task);
	}

	@Override
	public <V> Future<V> submitCallback(long token, Callable<V> callable) {
		DefaultFutureTask<V> task = new DefaultFutureTask<>(callable);
		TaskManager.getInstance().scheduleTask(task);
		return task;
	}

	@Override
	public void submitHandleEvent(GameEvent event) {
		EntityHandleEventTask task = EntityHandleEventTask.get(this, event);
		TaskManager.getInstance().scheduleTask(task);
	}
}
