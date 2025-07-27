package gleam.core;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import gleam.communication.MessageHandler;
import gleam.core.event.GameEvent;
import gleam.core.event.GameEventListener;
import gleam.task.Task;

/**
 * 实体
 * 
 * @author hdh
 *
 * @param <C> 该实体相关的组件
 */
public interface Entity<C extends Component> extends Component {

	/**
	 * 获取指定组件
	 * 
	 * @param <T>
	 * @param tag
	 * @return
	 */
	<T extends C> T getComponent(String tag);

	/**
	 * 唯一id
	 * 
	 * @return
	 */
	long getId();

	/**
	 * 提交执行任务<br>
	 * 使用该实体对应使用的线程执行<br>
	 * 异步/延后处理
	 * 
	 * @param task
	 */
	void submitTask(Task task);

	/**
	 * 提交回调任务<br>
	 * 使用该实体对应使用的线程执行<br>
	 * 若调用者为此实体 会使用当前线程之间执行
	 * 
	 * @param task
	 */
	<V> Future<V> submitCallback(long token, Callable<V> callable);

	/**
	 * 提交处理事件任务<br>
	 * 使用该实体对应线程执行<br>
	 * 异步/延后处理<br>
	 * 
	 * @param event
	 */
	void submitHandleEvent(GameEvent event);

	/**
	 * 注册组件
	 * 
	 * @param tag
	 * @param component
	 */
	void registerComponent(String tag, C component);

	/**
	 * 注册事件监听
	 * 
	 * @param eventId
	 * @param listener
	 */
	void registerEventListener(String eventId, GameEventListener listener);

	/**
	 * 注册协议处理
	 * 
	 * @param protocolId
	 * @param handler
	 */
	void registerMessageHandler(int protocolId, MessageHandler<?, ?> handler);

	/**
	 * 移除组件
	 * 
	 * @param tag
	 */
	void removeComponent(String tag);

	/**
	 * 移除事件监听
	 * 
	 * @param eventId
	 * @param listener
	 */
	void removeEventListener(String eventId, GameEventListener listener);

	/**
	 * 移除协议处理
	 * 
	 * @param protocolId
	 * @param handler
	 */
	void removeMessageHandler(int protocolId, MessageHandler<?, ?> handler);

}
