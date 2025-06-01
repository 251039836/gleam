package gleam.task;

/**
 * 任务<br>
 * 等价于{@link Runnable}
 * 
 * @author hdh
 *
 */
@FunctionalInterface
public interface Task {
    /**
     * 执行
     * 
     * @throws Exception
     */
    void execute() throws Exception;

    /**
     * 简介
     * 
     * @return
     */
    default String toDesc() {
        return getClass().getSimpleName();
    }

}
