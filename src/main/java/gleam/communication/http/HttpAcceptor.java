package gleam.communication.http;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.server.Acceptor;
import gleam.exception.ServerStarupError;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class HttpAcceptor implements Acceptor {

	private final static Logger logger = LoggerFactory.getLogger(HttpAcceptor.class);

	private final String name;
	private final InetSocketAddress address;

	private final ChannelInitializer<?> channelInitializer;

	private int bossThreadSize = 0;
	private int workerThreadSize = 0;

	private ServerBootstrap bootstrap;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	private boolean active;

	public HttpAcceptor(String name, InetSocketAddress address, ChannelInitializer<?> channelInitializer) {
		this.name = name;
		this.address = address;
		this.channelInitializer = channelInitializer;
	}

	public HttpAcceptor(String name, InetSocketAddress address, int bossThreadSize, int workerThreadSize,
			ChannelInitializer<?> channelInitializer) {
		this.name = name;
		this.address = address;
		this.bossThreadSize = bossThreadSize;
		this.workerThreadSize = workerThreadSize;
		this.channelInitializer = channelInitializer;
	}

	@Override
	public String getHost() {
		return address.getHostName();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPort() {
		return address.getPort();
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void listen() {
		ChannelFuture future = null;
		logger.info("httpAcceptor begin listen: address[{}]", address);
		try {
			ThreadFactory bossThreadFactory = new DefaultThreadFactory(name + "-boss-");
			ThreadFactory workerThreadFactory = new DefaultThreadFactory(name + "-worker-");
			bossGroup = new NioEventLoopGroup(bossThreadSize, bossThreadFactory);
			workerGroup = new NioEventLoopGroup(workerThreadSize, workerThreadFactory);

			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(channelInitializer);
			bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
			bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.SO_BACKLOG, 128);

			// 线程同步阻塞等待服务器绑定到指定端口
			future = bootstrap.bind(address).sync();
			future.channel().closeFuture().addListener(ChannelFutureListener.CLOSE);
			if (future.isSuccess()) {
				logger.info("httpAcceptor listening: address[{}]", address);
				active = true;
			} else {
				logger.info("httpAcceptor begin listening error: address[{}]", address);
			}
		} catch (Exception e) {
			active = false;
			logger.error("httpAcceptor[{}] begin listening error.", address, e);
			// 无法监听时 直接关闭进程
			throw new ServerStarupError("httpAcceptor listening error!");
		}
	}

	@Override
	public void shutdown() {
		active = false;
		logger.info("httpAcceptor shutdown: address[{}]", address);
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
	}

	public void setBootstrap(ServerBootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

	public void setBossGroup(EventLoopGroup bossGroup) {
		this.bossGroup = bossGroup;
	}

	public void setBossThreadSize(int bossThreadSize) {
		this.bossThreadSize = bossThreadSize;
	}

	public void setWorkerGroup(EventLoopGroup workerGroup) {
		this.workerGroup = workerGroup;
	}

	public void setWorkerThreadSize(int workerThreadSize) {
		this.workerThreadSize = workerThreadSize;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public ServerBootstrap getBootstrap() {
		return bootstrap;
	}

	public EventLoopGroup getBossGroup() {
		return bossGroup;
	}

	public int getBossThreadSize() {
		return bossThreadSize;
	}

	public ChannelInitializer<?> getChannelInitializer() {
		return channelInitializer;
	}

	public EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	public int getWorkerThreadSize() {
		return workerThreadSize;
	}
}
