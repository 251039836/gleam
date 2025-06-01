package gleam.communication.echo.handler;

import gleam.communication.MessageDirectHandler;
import gleam.communication.Protocol;
import gleam.communication.echo.protocol.EchoMessage;
import gleam.console.ConsoleManager;

/**
 * 处理平台命令
 *
 * @author redback
 * @version 1.00
 * @time 2020-4-26 14:48
 */
public class EchoMessageHandler implements MessageDirectHandler<EchoMessage> {
	@Override
	public int getReqId() {
		return EchoMessage.ID;
	}

	@Override
	public Protocol handleMessage(EchoMessage protocol) {
		String response = ConsoleManager.getInstance().handlerConsoleCmd(protocol.getContent());
		return EchoMessage.valueOf(response);
	}
}
