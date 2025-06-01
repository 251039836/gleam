package gleam.communication.client.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.authenticate.Identity;
import gleam.communication.client.Connector;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SocketConnector implements Connector {

	private final static Logger logger = LoggerFactory.getLogger(SocketConnector.class);
	/**
	 * 远端身份
	 */
	private final Identity remoteIdentity;
	/**
	 * 远端地址
	 */
	private final InetSocketAddress remoteAddress;

	private final ChannelInitializer<SocketChannel> channelInitializer;
	
	private Bootstrap bootstrap;

	private Channel channel;
	/**
	 * 当前连接状态
	 */
	private final AtomicInteger status = new AtomicInteger(STATUS_INIT);

	public SocketConnector(Identity remoteIdentity, InetSocketAddress remoteAddress,
			ChannelInitializer<SocketChannel> channelInitializer) {
		super();
		this.remoteIdentity = remoteIdentity;
		this.remoteAddress = remoteAddress;
		this.channelInitializer = channelInitializer;
	}

	@Override
	public void close() {
		status.set(STATUS_CLOSE);
		if (channel != null) {
			channel.close();
		}
	}

	@Override
	public void connect() {
		logger.info("[{}] address[{}] connecting.", remoteIdentity, remoteAddress);
		status.set(STATUS_CONNECTING);
		// 创建个线程进行监听
		String threadName = remoteIdentity + "_Contector";
		Thread thread = new Thread(() -> {
			doConnect();
		}, threadName);
		thread.start();
	}

	private void doConnect() {
		status.set(STATUS_CONNECTING);
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			bootstrap = new Bootstrap();
			bootstrap.group(group);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(channelInitializer);
			// 线程同步阻塞等连接到指定地址
			ChannelFuture future = bootstrap.connect(remoteAddress).sync();
			status.set(STATUS_CONNECTED);
			logger.info("[{}] address[{}] connected.", remoteIdentity, remoteAddress);
			// 成功连接到端口之后,给channel增加一个 管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束线程
			channel = future.channel();
			channel.closeFuture().sync();
		} catch (Exception e) {
			logger.error("connect[{}] address[{}] error.", remoteIdentity, remoteAddress, e);
		} finally {
			status.set(STATUS_DISCONNECT);
			group.shutdownGracefully();
		}
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public Identity getRemoteIdentity() {
		return remoteIdentity;
	}

	@Override
	public int getStatus() {
		return status.get();
	}

	@Override
	public boolean isActive() {
		if (status.get() != STATUS_CONNECTED) {
			return false;
		}
		if (channel == null) {
			return false;
		}
		if (!channel.isActive()) {
			return false;
		}
		return true;
	}

	@Override
	public void reconnect() {
		logger.info("[{}] address[{}] reconnecting.", remoteIdentity, remoteAddress);
		if (!status.compareAndSet(STATUS_DISCONNECT, STATUS_CONNECTING)) {
			// 不是断线状态 忽略
			return;
		}
		// 创建个线程进行监听
		String threadName = remoteIdentity + "_Contector";
		Thread thread = new Thread(() -> {
			doConnect();
		}, threadName);
		thread.start();
	}

}
