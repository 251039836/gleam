package gleam.communication.rpc.impl;

import gleam.communication.inner.protocol.ResInnerReturnCode;
import gleam.communication.rpc.ResponseCallback;

public abstract class AbstractSimpleResponseCallback implements ResponseCallback<ResInnerReturnCode> {

    @Override
    public void receiveResponse(ResInnerReturnCode response) {
        int returnCode = response.getCode();
        receiveReturnCode(returnCode);
    }

}
