package gleam.communication.authenticate;

import gleam.communication.Protocol;

/**
 * 链接身份验证器
 * 
 * @author hdh
 *
 * @param <T>
 */
public interface Authenticator<T extends Protocol> {

    /**
     * 身份验证<br>
     * 
     * @param protocol
     * @return
     */
    Identity authenticate(T protocol);

}
