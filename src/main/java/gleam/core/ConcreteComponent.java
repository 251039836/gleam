package gleam.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 抽象组件<br>
 * 
 * 
 * @author hdh
 *
 * @param <T> 组件拥有者
 */
public abstract class ConcreteComponent<T extends Entity<?>> implements Component {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 组件拥有者
	 */
	protected T owner;

	public T getOwner() {
		return owner;
	}

	public void setOwner(T owner) {
		this.owner = owner;
	}

	/**
	 * 注册事件监听到拥有者实体上
	 * 
	 * @param eventId
	 */
	protected void registerEventListener(String eventId) {
		owner.registerEventListener(eventId, this);
	}

	/**
	 * 注册消息处理到拥有者实体上
	 * 
	 * @param eventId
	 */
	protected void registerMessageHandler(int msgId) {
		owner.registerMessageHandler(msgId, this);
	}

	/**
	 * 移除事件监听
	 * 
	 * @param eventId
	 */
	protected void removeEventListener(String eventId) {
		owner.removeEventListener(eventId, this);
	}

	/**
	 * 移除消息处理
	 * 
	 * @param eventId
	 */
	protected void removeMessageHandler(int msgId) {
		owner.removeMessageHandler(msgId, this);
	}

}
