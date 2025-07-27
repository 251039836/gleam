package gleam.communication.impl;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.authenticate.Identity;
import gleam.communication.define.ConnectionConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public class NettyConnection implements Connection {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final Channel channel;

	private Identity identity;

	private long creationTime;

	private long heartbeatTime;

	public NettyConnection(Channel channel) {
		super();
		this.channel = channel;
		this.creationTime = System.currentTimeMillis();
	}

	@Override
	public <T> T getAttribute(AttributeKey<T> key) {
		Attribute<T> attr = channel.attr(key);
		return attr.get();
	}

	public Channel getChannel() {
		return channel;
	}

	@Override
	public long getCreationTime() {
		return creationTime;
	}

	@Override
	public long getHeartbeatTime() {
		return heartbeatTime;
	}

	@Override
	public String getId() {
		return channel.id().asLongText();
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public String getRemoteIp() {
		String ip = null;
		if (channel.hasAttr(ConnectionConstant.REAL_IP_ATTR_KEY)) {
			Attribute<String> realIpAttr = channel.attr(ConnectionConstant.REAL_IP_ATTR_KEY);
			ip = realIpAttr.get();
		}
		if (ip == null || ip.isEmpty()) {
			SocketAddress remoteAddress = channel.remoteAddress();
			if (remoteAddress instanceof InetSocketAddress inetAddress) {
				ip = inetAddress.getAddress().getHostAddress();
			} else {
				ip = remoteAddress.toString();
			}
		}
		return ip;
	}

	@Override
	public boolean isActive() {
		return channel.isActive();
	}

	@Override
	public boolean isClose() {
		return !channel.isOpen();
	}

	@Override
	public void sendMessage(ByteBuf message) {
		if (!channel.isActive()) {
			logger.debug("Id[{}] Identity[{}] not active", getId(), getIdentity());
			return;
		}
		channel.writeAndFlush(message);
	}

	@Override
	public void sendMessage(ByteBufHolder message) {
		if (!channel.isActive()) {
			logger.debug("Id[{}] Identity[{}] not active", getId(), getIdentity());
			return;
		}
		channel.writeAndFlush(message);
	}

	@Override
	public void sendProtocol(Protocol protocol) {
		if (!channel.isActive()) {
			logger.info("Id[{}] Identity[{}] not active.protocol[{}]", getId(), getIdentity(), protocol.getId());
			return;
		}
		channel.writeAndFlush(protocol);
	}

	@Override
	public void sendProtocolAndClose(Protocol protocol, int reason) {
		if (!channel.isActive()) {
			logger.info("Id[{}] Identity[{}] not active.protocol[{}]", getId(), getIdentity(), protocol.getId());
			close(reason);
			return;
		}
		ChannelFuture future = channel.writeAndFlush(protocol);
		future.addListener(new ChannelFutureListener() {

			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				close(reason);
			}
		});
	}

	@Override
	public Future<Void> close(int reason) {
		setAttribute(ConnectionConstant.CLOSE_REASON_ATTR_KEY, reason);
		ChannelFuture future = channel.close();
		return future;
	}

	@Override
	public <T> void setAttribute(AttributeKey<T> key, T value) {
		Attribute<T> attr = channel.attr(key);
		attr.set(value);
	}

	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}

	@Override
	public void setHeartbeatTime(long heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	@Override
	public void setIdentity(Identity identity) {
		this.identity = identity;
		Attribute<Identity> attr = channel.attr(Identity.ATTR_KEY);
		attr.set(identity);
	}

	@Override
	public String toFullName() {
		StringBuffer sb = new StringBuffer();
		sb.append("id[");
		sb.append(channel.id());
		sb.append("] ip[");
		sb.append(getRemoteIp());
		sb.append("]");
		if (identity != null) {
			sb.append("identity[");
			sb.append(identity);
			sb.append("]");
		}
		return sb.toString();
	}

}
