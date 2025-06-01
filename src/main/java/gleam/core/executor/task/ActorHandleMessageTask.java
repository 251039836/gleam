package gleam.core.executor.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.core.actor.Actor;
import gleam.task.Task;
import gleam.util.pool.ObjectPool;
import gleam.util.pool.Recoverable;

/**
 * actor处理消息任务
 * 
 * @author hdh
 *
 */
public class ActorHandleMessageTask implements Task, Recoverable {
	private final static Logger logger = LoggerFactory.getLogger(ActorHandleMessageTask.class);

	private final static ObjectPool<ActorHandleMessageTask> POOL = new ObjectPool<ActorHandleMessageTask>() {

		@Override
		protected ActorHandleMessageTask newObject() {
			return new ActorHandleMessageTask();
		}
	};

	public static ActorHandleMessageTask get(Actor<?> actor, Protocol request) {
		ActorHandleMessageTask task = POOL.get();
		task.setActor(actor);
		task.setRequest(request);
		return task;
	}

	private Actor<?> actor;

	private Protocol request;

	private ActorHandleMessageTask() {
	}

	@Override
	public void recycle() {
		this.actor = null;
		this.request = null;
	}

	@Override
	public void execute() throws Exception {
		Protocol response = null;
		try {
			response = actor.handleMessage(request);
		} catch (Exception e) {
			logger.error("actor[{}] handle message[{}] error.", actor.getId(), request.getClass().getName(), e);
		}
		if (response != null) {
			Connection connection = request.getConnection();
			if (connection != null) {
				response.setSeq(request.getSeq());
				connection.sendProtocol(response);
			}
		}
		POOL.recycle(this);
	}

	public Actor<?> getActor() {
		return actor;
	}

	public void setActor(Actor<?> actor) {
		this.actor = actor;
	}

	public Protocol getRequest() {
		return request;
	}

	public void setRequest(Protocol request) {
		this.request = request;
	}

	@Override
	public String toDesc() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName());
		sb.append(":");
		sb.append(actor.getId());
		sb.append("_");
		sb.append(request.getId());
		return sb.toString();
	}

}
