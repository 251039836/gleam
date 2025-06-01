package gleam.console;

/**
 *
 * 常用的回响消息
 *
 * @author redback
 * @version 1.00
 * @time 2020-4-26 15:15
 */
public enum ConsoleErrorMsg {

    /**
     * 命令为空
     */
    CMD_EMPTY("命令不能为空"),

    /**
     * 命令格式错误
     */
    CMD_FORMAT_ERROR("命令格式错误 : [%s]"),

    /**
     * 命令无法处理
     */
    COM_CANNOT_HANDLER("无法处理该命令类型 : [%s]"),

    /**
     * 执行成功
     */
    SUCCESS("执行成功!"),

    /**
     * 执行命令发生异常
     */
    INNER_ERROR("服务器内部发生错误");

    private String response;

    ConsoleErrorMsg(String response) {
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
