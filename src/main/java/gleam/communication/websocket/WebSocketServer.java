package gleam.communication.websocket;

import gleam.communication.server.ServerConnectionListener;
import gleam.communication.server.impl.SocketServer;
import gleam.communication.websocket.define.WebSocketConstant;
import gleam.communication.websocket.handler.WebSocketChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * websocket服务端<br>
 * 用于监听 处理玩家客户端请求
 * 
 * @author hdh
 *
 */
public abstract class WebSocketServer extends SocketServer {

	@Override
	protected ChannelInitializer<SocketChannel> buildChannelInitializer(
			ServerConnectionListener<?> connectionListener) {
		WebSocketChannelInitializer initializer = new WebSocketChannelInitializer(connectionListener);
		return initializer;
	}

	@Override
	protected long getHeartbeatTimeoutTime() {
		return WebSocketConstant.HEARTBEAT_TIMEOUT;
	}

}
