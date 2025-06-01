package gleam.communication.echo;

import gleam.communication.echo.protocol.BackstageHandlerResp;

/**
 * 后台消息处理器 处理器实现接口即可 泛型 T 指定为需要序列化的具体消息对象 具体协议的数据结构依赖于和后台的约定<br>
 * 
 * @see http://doc.inner.guangka.com/docs/houtai/houtai-1c35ks1780kme
 * @author redback
 * @version 1.00
 * @time 2020-5-8 16:27
 */
public interface BackstageMsgHandler<T> {

    /**
     * 返回协议 Id
     * 
     * @return protocolId
     */
    int getId();

    /**
     * 处理后台请求
     * 
     * @param msg 具体协议消息对象
     * @return JsonHandlerResp
     */
    BackstageHandlerResp handler(T msg);

}
