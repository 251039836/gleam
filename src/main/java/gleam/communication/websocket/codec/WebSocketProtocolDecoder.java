package gleam.communication.websocket.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.define.DisconnectReason;
import gleam.communication.protocol.factory.ProtocolFactory;
import gleam.communication.websocket.define.WebSocketConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;

/**
 * websocket加密协议解析类<br>
 * 若连接已登录 交换了秘钥 则后续客户端发送的协议都需要解密<br>
 * 用于前端的连接
 * 
 * ByteToMessageDecoder的子类都不可以在多个链接之间共享 不可添加@Sharable注解
 * 
 * @author hdh
 *
 */
@Sharable
public class WebSocketProtocolDecoder extends MessageToMessageDecoder<WebSocketFrame> {

	private final static Logger logger = LoggerFactory.getLogger(WebSocketProtocolDecoder.class);

	private void clientCloseConnection(Channel channel) {
		Attribute<Connection> connectionAttr = channel.attr(Connection.ATTR_KEY);
		Connection connection = connectionAttr.get();
		if (connection == null) {
			logger.info("client[{}] close websocket .", channel.id());
			return;
		}
		connection.close(DisconnectReason.CLIENT);
		logger.info("client[{}] close websocket.", connection.toFullName());

	}

	@Override
	protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
		if (msg instanceof BinaryWebSocketFrame) {
			decodeBinaryContent(ctx, msg, out);
		} else if (msg instanceof CloseWebSocketFrame) {
			// 前端主动关闭连接
			clientCloseConnection(ctx.channel());
		}

	}

	private void decodeBinaryContent(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) {
		ByteBuf buffer = msg.content();
		int remainLength = buffer.readableBytes();
		if (remainLength < WebSocketConstant.PROTOCOL_HEADER_LENGTH) {
			logger.warn("decode msg error.header length={},but realLength={} ",
					WebSocketConstant.PROTOCOL_HEADER_LENGTH, remainLength);
			return;
		}
		int seq = buffer.readInt();
		int id = buffer.readInt();
		Protocol protocol = ProtocolFactory.getProtocol(id);
		if (protocol == null) {
			logger.info("error protocol[{}]", id);
			return;
		}
		Channel channel = ctx.channel();
		try {
			if (channel.hasAttr(ProtocolEncrypt.ATTR_KEY)) {
				// 若已加密 则解密
				Attribute<ProtocolEncrypt> attr = channel.attr(ProtocolEncrypt.ATTR_KEY);
				ProtocolEncrypt encrypt = attr.get();
				encrypt.decode(buffer);
			}
			protocol.setSeq(seq);
			protocol.decode(buffer);
			out.add(protocol);
		} catch (Exception e) {
			// 协议解码出错 直接断连接
			Attribute<Connection> connectionAttr = channel.attr(Connection.ATTR_KEY);
			Connection connection = connectionAttr.get();
			if (connection != null) {
				logger.warn("channel[{}] protocol[{}] decode error.", connection.toFullName(), id, e);
				connection.close(DisconnectReason.DECODE_ERROR);
			} else {
				logger.warn("channel[{}] protocol[{}] decode error.", channel.id(), id, e);
				channel.close();
			}
		}
	}
}
