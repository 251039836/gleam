package gleam.communication.task;

import gleam.task.TaskManager;

/**
 * 通信层专用的任务管理器<br>
 * 内网服务器之间的链接 因netty的线程模型影响 2端都是单线程处理该链接接收到的协议<br>
 * 当同1链接2端都在ask对方时将会死锁>
 * 不可以用netty自带线程池处理 单个链接视为1个消息队列对应1个线程<br>
 * 双向ask会导致死锁
 * 
 * @author hdh
 *
 */
public class CommunicationTaskManager {
	/**
	 * 只处理通信层定时任务
	 */
	public static final TaskManager TIMER = TaskManager.buildSmallInstance("CommunicationTimer");
	/**
	 * 只处理内网链接中服务端的任务
	 */
	public static final TaskManager SERVER = TaskManager.buildSmallInstance("ServerTask");
	/**
	 * 只处理内网链接中客户端的任务
	 */
	public static final TaskManager CLIENT = TaskManager.buildSmallInstance("ClientTask");

}
