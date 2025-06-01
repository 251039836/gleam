package gleam.communication.http.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.http.dispatcher.DispatcherServlet;
import gleam.communication.http.helper.HttpServerHelper;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * 简单的http服务器连接处理类
 * 
 * @author hdh
 */
@Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	private final static Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

	private DispatcherServlet servlet;

	public void init() {
		servlet = new DispatcherServlet();
		servlet.init();
	}

	/**
	 * 建立连接
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.debug("http channel[{}] connected.", ctx.channel().remoteAddress());

	}

	/**
	 * 关闭连接
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.debug("http channel[{}] disconnected.", ctx.channel().remoteAddress());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("http channel[{}] exceptionCaught:{}:{}", ctx.channel().remoteAddress(),
				cause.getClass().getName(), cause.getMessage());
		if (cause instanceof TooLongFrameException) {
			sendErrorAndClose(ctx, HttpResponseStatus.BAD_REQUEST);
		} else if (cause instanceof IllegalArgumentException) {
			sendErrorAndClose(ctx, HttpResponseStatus.NOT_FOUND);
		} else {
			sendErrorAndClose(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (!request.decoderResult().isSuccess()) {
			sendErrorAndClose(ctx, HttpResponseStatus.BAD_REQUEST);
			return;
		}
		FullHttpResponse response = servlet.processRequest(ctx.channel(), request);
		ChannelFuture future = ctx.writeAndFlush(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 发送错误消息并关闭连接
	 * 
	 * @param ctx
	 * @param status
	 */
	private void sendErrorAndClose(ChannelHandlerContext ctx, HttpResponseStatus status) {
		FullHttpResponse response = HttpServerHelper.createResponse(status);
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

}
