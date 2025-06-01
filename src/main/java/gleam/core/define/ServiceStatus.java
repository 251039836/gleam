package gleam.core.define;

/**
 * 服务状态
 * 
 * @author hdh
 *
 */
public enum ServiceStatus {
    /**
     * 原始状态
     */
    ORIGINAL,
    /**
     * 初始化中
     */
    INITIALIZING,
    /**
     * 初始化完成<br>
     */
    INITIALIZED,
    /**
     * 启动中<br>
     */
    STARTING,
    /**
     * 启动成功<br>
     * 开始提供服务
     */
    STARTED,
    /**
     * 关闭<br>
     * 停止服务
     */
    STOPPING,
    /**
     * 已停止
     */
    STOPPED,
    /**
     * 销毁<br>
     * 清理数据
     */
    DESTROYING,
    /**
     * 已销毁
     */
    DESTROYED
}
