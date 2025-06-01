package gleam.communication.rpc;

import java.util.concurrent.ExecutionException;

import gleam.communication.Protocol;
import gleam.communication.define.ConnectionConstant;
import gleam.communication.rpc.impl.RpcFutureResult;
import gleam.exception.RpcException;

/**
 * 支持rpc操作的终端
 * 
 * @author hdh
 *
 */
public interface RpcEndpoint {

	/**
	 * 询问服务端<br>
	 * 发送消息 返回返回消息/返回码的future<br>
	 * 
	 * @param <R>
	 * @param request
	 * @param timeout
	 * @return
	 */
	<R extends Protocol> RpcFutureResult<R> ask(Protocol request, long timeout);

	/**
	 * 询问服务端<br>
	 * 发送消息 返回返回消息/返回码的future<br>
	 * 
	 * @param <R>
	 * @param request
	 * @return
	 */
	<R extends Protocol> RpcFutureResult<R> ask(Protocol request);

	/**
	 * 询问服务端<br>
	 * 发送消息 若有回调/超时 则执行callback<br>
	 * {@link ConnectionConstant#DEFAULT_RPC_TIMEOUT}
	 * 
	 * @param <R>      response
	 * @param request
	 * @param callback
	 */
	<R extends Protocol> void ask(Protocol request, ResponseCallback<R> callback);

	/**
	 * 询问服务端<br>
	 * 发送消息 若有回调/超时 则执行callback
	 * 
	 * @param <T>      response
	 * @param request
	 * @param timeout
	 * @param callback
	 */
	<R extends Protocol> void ask(Protocol request, long timeout, ResponseCallback<R> callback);

	/**
	 * 跨节点转发询问服务端<br>
	 * 发送消息 返回返回消息的future<br>
	 * future若抛出{@link RpcException} 该错误会被{@link ExecutionException}包裹<br>
	 * {@link ConnectionConstant#DEFAULT_RPC_TIMEOUT}
	 * 
	 * @param <R>
	 * @param dstServerType
	 * @param dstServerId
	 * @param request
	 * @return
	 */
	<R extends Protocol> RpcFutureResult<R> forwardAsk(int dstServerType, int dstServerId, Protocol request);

	/**
	 * 跨节点转发询问服务端<br>
	 * 发送消息 返回返回消息的future<br>
	 * future若抛出{@link RpcException} 该错误会被{@link ExecutionException}包裹
	 * 
	 * @param <R>           response
	 * @param dstServerType
	 * @param dstServerId
	 * @param request
	 * @param timeout
	 * @return
	 */
	<R extends Protocol> RpcFutureResult<R> forwardAsk(int dstServerType, int dstServerId, Protocol request,
			long timeout);

	/**
	 * 跨节点转发询问服务端<br>
	 * 发送消息 若有回调/超时 则执行callback<br>
	 * {@link ConnectionConstant#DEFAULT_RPC_TIMEOUT}
	 * 
	 * @param <R>           response
	 * @param dstServerType
	 * @param dstServerId
	 * @param request
	 * @param callback
	 */
	<R extends Protocol> void forwardAsk(int dstServerType, int dstServerId, Protocol request,
			ResponseCallback<R> callback);

	/**
	 * 跨节点转发询问服务端<br>
	 * 发送消息 若有回调/超时 则执行callback
	 * 
	 * @param <T>           response
	 * @param dstServerType
	 * @param dstServerId
	 * @param request
	 * @param timeout
	 * @param callback
	 */
	<R extends Protocol> void forwardAsk(int dstServerType, int dstServerId, Protocol request, long timeout,
			ResponseCallback<R> callback);

}
