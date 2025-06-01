package gleam.communication.echo;

import java.net.InetSocketAddress;

import gleam.communication.echo.handler.EchoChannelInitializer;
import gleam.communication.echo.listener.EchoServerConnectListener;
import gleam.communication.server.Acceptor;
import gleam.communication.server.ServerConnectionListener;
import gleam.communication.server.impl.SocketServer;
import gleam.communication.server.impl.SocketAcceptor;
import gleam.config.ServerSettings;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public abstract class EchoServer extends SocketServer {

	public final static String CONSOLE_HOST_KEY = "console.host";
	public final static String CONSOLE_PORT_KEY = "console.port";

	@Override
	protected Acceptor buildAcceptor(ChannelInitializer<SocketChannel> channelInitializer) {
		// 该服务器不需要开启过多线程监听/处理
		return new SocketAcceptor(getName(), address, 1, 2, channelInitializer);
	}

	@Override
	protected ChannelInitializer<SocketChannel> buildChannelInitializer(
			ServerConnectionListener<?> connectionListener) {
		EchoChannelInitializer initializer = new EchoChannelInitializer(connectionListener);
		return initializer;
	}

	@Override
	protected ServerConnectionListener<?> buildConnectionListener() {
		return new EchoServerConnectListener(this);
	}

	@Override
	protected InetSocketAddress loadAddress() {
		String host = ServerSettings.getProperty(CONSOLE_HOST_KEY);
		int port = ServerSettings.getIntProperty(CONSOLE_PORT_KEY);
		InetSocketAddress address = new InetSocketAddress(host, port);
		return address;
	}

}
