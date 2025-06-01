package gleam.task.token;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import gleam.task.Task;

/**
 * 只是{@link FutureTask}的自定义{@link Task}的实现
 * 
 * @author hdh
 *
 * @param <V>
 */
public class DefaultFutureTask<V> extends FutureTask<V> implements Task {

    public DefaultFutureTask(Callable<V> callable) {
        super(callable);
    }

    @Override
    public void execute() {
        run();
    }

}
