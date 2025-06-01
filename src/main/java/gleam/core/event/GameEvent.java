package gleam.core.event;

import org.apache.commons.lang3.StringUtils;

/**
 * 游戏事件<br>
 * 消息应当是不可变的<br>
 * 不可在处理消息过程中进行修改<br>
 * 
 * @author hdh
 *
 */
public interface GameEvent {

	/**
	 * 事件id<br>
	 * 目前直接用class.getSimpleName();
	 * 
	 * @return
	 */
	String getId();

	/**
	 * 判断该事件id是否属于该事件
	 * 
	 * @param eventId
	 * @return
	 */
	default boolean isThisEvent(String eventId) {
		return StringUtils.equals(eventId, getId());
	}

}
