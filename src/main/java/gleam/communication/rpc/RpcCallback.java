package gleam.communication.rpc;

import gleam.communication.Protocol;

/**
 * rpc回调
 * 
 * @author hdh
 *
 * @param <T> 返回的消息
 */
public interface RpcCallback<T extends Protocol> {
    /**
     * 消息序号
     * 
     * @return
     */
    int getSeq();

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

    /**
     * 是否超时
     * 
     * @param now
     * @return
     */
    boolean isTimeout(long now);

}
