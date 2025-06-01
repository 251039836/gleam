package gleam.core.actor;

import gleam.core.Component;
import gleam.core.Entity;
import gleam.task.token.TokenTaskQueue;

/**
 * actor模型的简化版本<br>
 * 该actor的消息队列对应{@link TokenTaskQueue}<br>
 * 处理外部消息/事件时 应该都使用{@link Actor#submitTask(gleam.task.Task)}扔到该actor对应的线程中处理
 * 以避免并发问题
 * 
 * @author hdh
 *
 * @param <T>
 */
public interface Actor<T extends Component> extends Entity<T> {

}
