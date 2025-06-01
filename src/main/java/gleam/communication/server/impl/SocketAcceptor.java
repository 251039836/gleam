package gleam.communication.server.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.server.Acceptor;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class SocketAcceptor implements Acceptor {

	private final static Logger logger = LoggerFactory.getLogger(SocketAcceptor.class);

	private final String name;
	private final InetSocketAddress address;

	private final ChannelInitializer<SocketChannel> channelInitializer;

	private int bossThreadSize = 0;
	private int workerThreadSize = 0;

	private ServerBootstrap bootstrap;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	private boolean active;

	public SocketAcceptor(String name, InetSocketAddress address, ChannelInitializer<SocketChannel> channelInitializer) {
		this.name = name;
		this.address = address;
		this.channelInitializer = channelInitializer;
	}

	public SocketAcceptor(String name, InetSocketAddress address, int bossThreadSize, int workerThreadSize,
			ChannelInitializer<SocketChannel> channelInitializer) {
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
		logger.info("acceptor begin listen: address[{}]", address);
		ThreadFactory bossThreadFactory = new DefaultThreadFactory(name + "-boss-");
		ThreadFactory workerThreadFactory = new DefaultThreadFactory(name + "-worker-");
		bossGroup = new NioEventLoopGroup(bossThreadSize, bossThreadFactory);
		workerGroup = new NioEventLoopGroup(workerThreadSize, workerThreadFactory);
		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(channelInitializer);
			// 线程同步阻塞等待服务器绑定到指定端口
			ChannelFuture channelFuture = bootstrap.bind(address).sync();
			logger.info("acceptor listening: address[{}]", address);
			active = true;
			// 成功绑定到端口之后,给channel增加一个 管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程
			channelFuture.channel().closeFuture().sync();
			// channelFuture.await();
		} catch (Exception e) {
			active = false;
			logger.error("address[{}] begin listening error.", address, e);
			// 无法监听时 直接关闭进程
			System.exit(1);
		} finally {
			active = false;
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			logger.info("acceptor close: address[{}]", address);
		}
	}

	@Override
	public void shutdown() {
		active = false;
		logger.info("acceptor shutdown: address[{}]", address);
		if (bossGroup != null) {
			bossGroup.shutdownGracefully();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully();
		}
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

	public ChannelInitializer<SocketChannel> getChannelInitializer() {
		return channelInitializer;
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

	public EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	public int getWorkerThreadSize() {
		return workerThreadSize;
	}

}
