package gleam.communication;

/**
 * 消息直接处理类<br>
 * 该处理类只对应1个消息<br>
 * 接受到消息时 直接进行处理
 * 
 * @author hdh
 *
 * @param <Q> request
 * @param <R> response
 */
public interface MessageDirectHandler<Q extends Protocol> extends MessageHandler<Q, Protocol> {

	/**
	 * 消息id
	 * 
	 * @return
	 */
	int getReqId();

	/**
	 * 处理消息
	 * 
	 * @param connection
	 * @param request
	 * @return 该消息对应的返回协议 可能为空
	 */
	@Override
	Protocol handleMessage(Q protocol);
}
