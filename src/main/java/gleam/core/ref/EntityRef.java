package gleam.core.ref;

import java.util.concurrent.ExecutionException;

import gleam.communication.Protocol;
import gleam.communication.rpc.ResponseCallback;
import gleam.communication.rpc.impl.RpcFutureResult;
import gleam.core.ref.define.EntityType;
import gleam.exception.RpcException;

/**
 * 实体引用<br>
 * 一套简单的类似akka的actorRef实现<br>
 * 只实现寻址通信部分 不涉及线程模型/并发操作<br>
 * 用于封装对本地/远程的实体的rpc操作<br>
 * 
 * akka整套框架太大太复杂了 万物皆actor 所有操作都在actor中执行也不太适合游戏服的场景<br>
 * 此处只是实现将实体做成不完整的actor<br>
 * 
 * @author hdh
 */
public interface EntityRef {
	/**
	 * 实体id
	 * 
	 * @return
	 */
	long getId();

	/**
	 * 实体类型<br>
	 * {@link EntityType}
	 * 
	 * @return
	 */
	int getEntityType();

	/**
	 * 发送消息给该实体<br>
	 * 不在意是否发送成功
	 * 
	 * @param message
	 */
	void tell(Protocol message);

	/**
	 * 询问指向的实体<br>
	 * 发送消息 返回返回消息的future<br>
	 * future若抛出{@link RpcException} 该错误会被{@link ExecutionException}包裹
	 * 
	 * @param <R>     response
	 * @param request
	 * @param timeout
	 * @return
	 */
	<R extends Protocol> RpcFutureResult<R> ask(Protocol request, long timeout);

	/**
	 * 询问指向的实体<br>
	 * 发送消息 若有回调/超时 则执行callback
	 * 
	 * @param <T>      response
	 * @param request
	 * @param timeout
	 * @param callback
	 */
	<R extends Protocol> void ask(Protocol request, long timeout, ResponseCallback<R> callback);

}
