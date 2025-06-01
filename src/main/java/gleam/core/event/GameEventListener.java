package gleam.core.event;

/**
 * 事件监听类接口
 * 
 * @author hdh
 *
 */
@FunctionalInterface
public interface GameEventListener {

    /**
     * 处理事件
     * 
     * @param gameEvent
     */
    void handleGameEvent(GameEvent gameEvent);

}
