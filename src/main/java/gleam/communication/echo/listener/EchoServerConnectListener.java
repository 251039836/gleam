package gleam.communication.echo.listener;

import gleam.communication.Connection;
import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.define.DisconnectReason;
import gleam.communication.echo.EchoServer;
import gleam.communication.echo.handler.EchoMessageHandler;
import gleam.communication.server.ServerConnectionListener;

/**
 * @author redback
 * @version 1.00
 * @time 2020-4-26 12:10
 */
public class EchoServerConnectListener extends ServerConnectionListener<EchoServer> {

	public EchoServerConnectListener(EchoServer server) {
		super(server);
	}

	@Override
	public void init() {
		registerDirectHandler(new EchoMessageHandler());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void receiveProtocol(Connection connection, Protocol protocol) {
		int msgId = protocol.getId();
		MessageDirectHandler messageHandler = directHandlers.get(msgId);
		Protocol response = null;
		if (messageHandler != null) {
			response = messageHandler.handleMessage(protocol);
			if (response != null) {
				response.setSeq(protocol.getSeq());
				// 20220223 和后台/大数据的通信 应答结束后 服务端主动关闭链接
				connection.sendProtocolAndClose(response, DisconnectReason.OVER);
			}
		}
	}
}
