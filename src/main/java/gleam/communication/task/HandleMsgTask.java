package gleam.communication.task;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.core.Entity;
import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

public class HandleMsgTask implements Task, Recoverable {

	private final static ObjectPool<HandleMsgTask> POOL = new ObjectPool<HandleMsgTask>() {

		@Override
		protected HandleMsgTask newObject() {
			return new HandleMsgTask();
		}
	};

	public static HandleMsgTask get(Entity<?> entity, Connection connection, Protocol protocol) {
		HandleMsgTask task = POOL.obtain();
		task.setEntity(entity);
		task.setConnection(connection);
		task.setProtocol(protocol);
		return task;
	}

	private Entity<?> entity;

	private Connection connection;

	private Protocol protocol;

	@Override
	public void execute() throws Exception {
		Protocol response = entity.handleMessage(protocol);
		if (response != null) {
			response.setSeq(-protocol.getSeq());
			connection.sendProtocol(response);
		}
		POOL.recycle(this);
	}

	@Override
	public void recycle() {
		entity = null;
		connection = null;
		protocol = null;
	}

	@Override
	public String toDesc() {
		StringBuffer sb = new StringBuffer();
		sb.append(entity.getClass().getSimpleName()).append(entity.getId());
		sb.append("_handleMsg_");
		sb.append(protocol.getId());
		return sb.toString();
	}

	public Entity<?> getEntity() {
		return entity;
	}

	public void setEntity(Entity<?> entity) {
		this.entity = entity;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

}
