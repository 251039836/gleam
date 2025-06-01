package gleam.communication.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.ConnectionListener;
import gleam.communication.Protocol;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;

/**
 * 默认连接处理类<br>
 * 链接监听器的封装类<br>
 * 将netty的消息转发到逻辑层中
 * 
 * @author hdh
 *
 */
@Sharable
public class CommonChannelHandler extends SimpleChannelInboundHandler<Protocol> {

    private final static Logger logger = LoggerFactory.getLogger(CommonChannelHandler.class);

    private final ConnectionListener connectionListener;

    public CommonChannelHandler(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    /**
     * 建立链接
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Channel channel = ctx.channel();
        Connection connection = new NettyConnection(channel);
        Attribute<Connection> connectionAttr = channel.attr(Connection.ATTR_KEY);
        connectionAttr.set(connection);
        connectionListener.connected(connection);
    }

    /**
     * 关闭链接
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel channel = ctx.channel();
        Attribute<Connection> connectionAttr = channel.attr(Connection.ATTR_KEY);
        Connection connection = connectionAttr.get();
        if (connection == null) {
            logger.info("channel[{}] disconnected.", channel.id().asLongText());
            return;
        }
        connectionListener.disconnected(connection);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Protocol msg) throws Exception {
        Channel channel = ctx.channel();
        Attribute<Connection> connectionAttr = channel.attr(Connection.ATTR_KEY);
        Connection connection = connectionAttr.get();
        if (connection == null) {
            logger.error("channel[{}] receiveProtocol[{}] fail.connection is null.", channel.id().asLongText(), msg.getId());
            return;
        }
        try {
        	msg.setConnection(connection);
            connectionListener.receiveProtocol(connection, msg);
        } catch (Exception e) {
            logger.error("receive protocol[{}] error.", msg.getId(), e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        Attribute<Connection> connectionAttr = channel.attr(Connection.ATTR_KEY);
        Connection connection = connectionAttr.get();
        if (connection == null) {
            logger.error("channel[{}] caught exception:", channel.id().asLongText(), cause);
            return;
        }
        connectionListener.exceptionCaught(connection, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event == null) {
            return;
        }
        Channel channel = ctx.channel();
        Attribute<Connection> connectionAttr = channel.attr(Connection.ATTR_KEY);
        Connection connection = connectionAttr.get();
        if (connection == null) {
            logger.warn("channel[{}] userEventTriggered[{}] fail.connection is null.", channel.id().asLongText(), event.getClass().getName());
            return;
        }
        connectionListener.handleTriggerEvent(connection, event);
    }

}
