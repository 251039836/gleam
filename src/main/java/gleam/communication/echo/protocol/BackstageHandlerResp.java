package gleam.communication.echo.protocol;

/**
 *
 * 后台 handler 的处理回复消息，不可变类 不再直接提供构造返回 json 的工具类了或者方法了 json handler 尽可能不要知道其他类的存在
 *
 * @author redback
 * @version 1.00
 * @time 2020-5-9 15:02
 */
public class BackstageHandlerResp {

    /**
     * 成功错误码
     */
    private static final int ERROR_CODE_SUCCESS = 0;
    /**
     * 失败错误码
     */
    private static final int ERROR_CODE_FAILED = -1;

    /**
     * 构造失败消息，内容为空
     * 
     * @return
     */
    public static BackstageHandlerResp failed() {
        return failed(null);
    }

    /**
     * 构造完整成功消息
     * 
     * @param msg 协议的返还内容
     * @return JsonHandlerResp
     */
    public static BackstageHandlerResp failed(Object msg) {
        return new BackstageHandlerResp(ERROR_CODE_FAILED, msg);
    }

    /**
     * 构造成功消息，内容为空
     * 
     * @return
     */
    public static BackstageHandlerResp success() {
        return success(null);
    }

    /**
     * 构造完整失败消息
     * 
     * @param msg 协议的返还内容
     * @return JsonHandlerResp
     */
    public static BackstageHandlerResp success(Object msg) {
        return new BackstageHandlerResp(ERROR_CODE_SUCCESS, msg);
    }

    /**
     * 错误码
     */
    private final int code;

    /**
     * 协议回应的其他消息，没有传 null
     */
    private final Object msg;

    private BackstageHandlerResp(int code, Object msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public Object getMsg() {
        return msg;
    }
}
