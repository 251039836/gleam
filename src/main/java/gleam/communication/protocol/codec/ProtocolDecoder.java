package gleam.communication.protocol.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.protocol.factory.ProtocolFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.Attribute;

/**
 * 协议解析类<br>
 * 用于内连接<br>
 * 
 * ByteToMessageDecoder的子类都不可以在多个链接之间共享 不可添加@Sharable注解
 * 
 * @author hdh
 * 
 */
public class ProtocolDecoder extends ByteToMessageDecoder {

    private final static Logger logger = LoggerFactory.getLogger(ProtocolDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 8) {
            return;
        }
        int seq = in.readInt();
        int id = in.readInt();
        Protocol protocol = ProtocolFactory.getProtocol(id);
        if (protocol == null) {
            // 未注册过的协议
            logger.info("error protocol[{}]", id);
            // FIXME 要不要重置下?
            in.clear();
            return;
        }
        try {
            protocol.setSeq(seq);
            protocol.decode(in);
            out.add(protocol);
        } catch (Exception e) {
            Channel channel = ctx.channel();
            Attribute<Connection> connectionAttr = channel.attr(Connection.ATTR_KEY);
            Connection connection = connectionAttr.get();
            if (connection != null) {
                // 内连接 解码出错 不断连接
                logger.error("channel[{}] protocol[{}] decode error.", connection.toFullName(), id, e);
            } else {
                // 未登陆验证过的连接 断开链接
                logger.error("channel[{}] protocol[{}] decode error.", channel.id(), id, e);
                channel.close();
            }
        }
    }

}
