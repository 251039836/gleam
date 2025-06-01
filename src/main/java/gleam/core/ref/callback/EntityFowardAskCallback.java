package gleam.core.ref.callback;

import gleam.communication.Connection;
import gleam.communication.Protocol;
import gleam.communication.inner.protocol.ResInnerReturnCode;
import gleam.communication.protocol.ProtocolUtil;
import gleam.communication.rpc.ResponseCallback;
import gleam.core.define.BasicErrorCode;
import gleam.core.ref.protocol.ReqEntityForward;
import gleam.core.ref.protocol.ResEntityForward;
import gleam.exception.ErrorCodeException;

public class EntityFowardAskCallback implements ResponseCallback<Protocol> {

	private ReqEntityForward request;

	public EntityFowardAskCallback(ReqEntityForward request) {
		super();
		this.request = request;
	}

	@Override
	public void receiveResponse(Protocol response) {
		ResEntityForward fowardResponse = buildFowardResponse(response);
		Connection connection = request.getConnection();
		connection.sendProtocol(fowardResponse);
	}

	@Override
	public void receiveReturnCode(int returnCode) {
		ResInnerReturnCode innerProtocol = new ResInnerReturnCode();
		innerProtocol.setCode(returnCode);
		ResEntityForward fowardResponse = buildFowardResponse(innerProtocol);
		Connection connection = request.getConnection();
		connection.sendProtocol(fowardResponse);
	}

	@Override
	public void handleException(Exception ex) {
		int errorCode = BasicErrorCode.UNKNOW_ERROR;
		if (ex instanceof ErrorCodeException ece) {
			errorCode = ece.getErrorCode();
		} else {
			Throwable cause = ex.getCause();
			if (cause != null && cause instanceof ErrorCodeException) {
				ErrorCodeException errorCodeException = (ErrorCodeException) cause;
				errorCode = errorCodeException.getErrorCode();
			}
		}
		ResInnerReturnCode innerProtocol = new ResInnerReturnCode();
		innerProtocol.setCode(errorCode);
		ResEntityForward fowardResponse = buildFowardResponse(innerProtocol);
		Connection connection = request.getConnection();
		connection.sendProtocol(fowardResponse);
	}

	public ReqEntityForward getRequest() {
		return request;
	}

	public void setRequest(ReqEntityForward request) {
		this.request = request;
	}

	private ResEntityForward buildFowardResponse(Protocol protocol) {
		ResEntityForward response = new ResEntityForward();
		response.setSrcServerType(request.getSrcServerType());
		response.setSrcServerId(request.getSrcServerId());
		response.setDstServerType(request.getDstServerType());
		response.setDstServerId(request.getDstServerId());
		response.setDstEntityType(request.getDstEntityType());
		response.setDstEntityId(request.getDstEntityId());
		response.setForwardMsgSeq(-request.getForwardMsgSeq());
		response.setForwardMsgId(protocol.getId());
		byte[] forwardMsgData = ProtocolUtil.encodeMessage(protocol);
		response.setForwardMsgData(forwardMsgData);
		return response;
	}
}
