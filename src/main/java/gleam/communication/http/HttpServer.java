package gleam.communication.http;

import java.net.InetSocketAddress;

import gleam.communication.http.handler.HttpChannelInitializer;
import gleam.communication.http.handler.HttpServerHandler;
import gleam.communication.server.Acceptor;
import gleam.communication.server.ConnectionManager;
import gleam.communication.server.Server;
import gleam.config.ServerSettings;
import gleam.core.service.AbstractService;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpServer extends AbstractService implements Server {

	private static HttpServer instance = new HttpServer();

	public static HttpServer getInstance() {
		return instance;
	}

	public final static String HTTP_HOST_KEY = "http.host";
	public final static String HTTP_PORT_KEY = "http.port";
	/**
	 * 监听地址
	 */
	protected InetSocketAddress address;
	/**
	 * 接收器
	 */
	protected Acceptor acceptor;
	/**
	 * 链接管理类
	 */
	protected final ConnectionManager connectionManager;

	public HttpServer() {
		this.connectionManager = buildConnectionManger();
	}

	@Override
	public int getPriority() {
		return PRIORITY_LOW;
	}

	@Override
	public void onInitialize() {
		this.address = loadAddress();
		SimpleChannelInboundHandler<FullHttpRequest> httpServerHandler = buildHttpServerHandler();
		ChannelInitializer<SocketChannel> channelInitializer = buildChannelInitializer(httpServerHandler);
		this.acceptor = buildAcceptor(channelInitializer);
	}

	@Override
	public void onStart() {
		String threadName = getName() + "_Acceptor";
		Thread thread = new Thread(() -> {
			acceptor.listen();
		}, threadName);
		thread.start();
		// 阻塞到成功监听
		try {
			while (!acceptor.isActive()) {
				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
			logger.error(getName() + " startup error.", e);
			System.exit(1);
		}
	}

	@Override
	public void onStop() {
		if (acceptor != null) {
			acceptor.shutdown();
		}
	}

	@Override
	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public Acceptor getAcceptor() {
		return acceptor;
	}

	@Override
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	protected InetSocketAddress loadAddress() {
		String host = ServerSettings.getProperty(HTTP_HOST_KEY);
		int port = ServerSettings.getIntProperty(HTTP_PORT_KEY);
		InetSocketAddress address = new InetSocketAddress(host, port);
		return address;
	}

	/**
	 * 创建接收器
	 * 
	 * @param channelInitializer
	 * @return
	 */
	protected Acceptor buildAcceptor(ChannelInitializer<SocketChannel> channelInitializer) {
		InetSocketAddress address = getAddress();
		String name = getName();
		Acceptor acceptor = new HttpAcceptor(name, address, channelInitializer);
		return acceptor;
	}

	public ConnectionManager buildConnectionManger() {
		return new ConnectionManager();
	}

	protected ChannelInitializer<SocketChannel> buildChannelInitializer(
			SimpleChannelInboundHandler<FullHttpRequest> httpServerHandler) {
		return new HttpChannelInitializer(httpServerHandler);
	}

	private SimpleChannelInboundHandler<FullHttpRequest> buildHttpServerHandler() {
		HttpServerHandler handler = new HttpServerHandler();
		handler.init();
		return handler;
	}
}
