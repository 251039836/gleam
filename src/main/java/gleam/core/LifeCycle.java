package gleam.core;

public interface LifeCycle {

    /**
     * 初始化
     */
    void initialize();

    /**
     * 开始
     */
    void start();

    /**
     * 停止
     */
    void stop();

    /**
     * 销毁
     */
    void destroy();
}
