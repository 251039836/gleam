package gleam.communication;

/**
 * 消息处理类
 * 
 * @author hdh
 *
 * @param <Q> request
 * @param <R> response
 */
@FunctionalInterface
public interface MessageHandler<Q extends Protocol, R extends Protocol> {

	/**
	 * 处理消息
	 * 
	 * @param request
	 * @return 该消息对应的返回协议 可能为空
	 */
	R handleMessage(Q request);

}
