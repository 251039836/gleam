package gleam.communication.rpc;

import gleam.communication.Protocol;

/**
 * rpc返回消息的回调
 * 
 * @author hdh
 *
 * @param <T>
 */
public interface ResponseCallback<T extends Protocol> {

    /**
     * 接受到回调消息
     * 
     * @param response
     */
    void receiveResponse(T response);

    /**
     * 接受到返回码消息
     * 
     * @param returnCode
     */
    void receiveReturnCode(int returnCode);

    /**
     * 处理错误
     * 
     * @param ex
     */
    void handleException(Exception ex);

}
