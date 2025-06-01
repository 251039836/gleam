package gleam.communication.http.dispatcher;

import java.io.IOException;

import gleam.communication.http.annotation.RequestMapping;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

/**
 * http请求处理器
 * 
 * @author hdh
 */
public interface HttpProcessor {

	/**
	 * 对应{@link RequestMapping}
	 * 
	 * @return
	 */
	String getUrl();

	/**
	 * 处理请求
	 * 
	 * @param channel
	 * @param request
	 * @return 返回请求结果
	 * @throws IOException 
	 */
	FullHttpResponse processRequest(Channel channel, FullHttpRequest request) throws IOException;

}
