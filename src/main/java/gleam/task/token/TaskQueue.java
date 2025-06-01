package gleam.task.token;

import gleam.task.Task;

/**
 * 任务队列
 * 
 * @author hdh
 *
 */
public interface TaskQueue extends Runnable {

    void addTask(Task task);

    /**
     * 若任务执行中 获取当前队列线程
     * 
     * @return
     */
    Thread getRunThread();

}
