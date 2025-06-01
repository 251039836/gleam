package gleam.core.service;

import gleam.core.Entity;

/**
 * 分区<br>
 * 某个玩法的分组 或者某个进程的上下文<br>
 * 可能会多线程处理/分发事件
 * 
 * 
 * @author hdh
 */
public interface Zone<T extends Service> extends Entity<T>, Service {

}
