package gleam.exception.handler;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 线程默认的未捕获错误的处理类<br>
 * 只打印严重报错日志 不做其他处理
 * 
 * @author hdh
 *
 */
public class DefaultUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private static DefaultUncaughtExceptionHandler instance = new DefaultUncaughtExceptionHandler();

    private final static Logger logger = LoggerFactory.getLogger(DefaultUncaughtExceptionHandler.class);

    public static DefaultUncaughtExceptionHandler getInstance() {
        return instance;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("thread[{}] uncaughtException.", t.getName(), e);
    }

}
