package gleam.communication.authenticate;

import io.netty.util.AttributeKey;

/**
 * 身份
 * 
 * @author hdh
 *
 */
public interface Identity {

    AttributeKey<Identity> ATTR_KEY = AttributeKey.valueOf(Identity.class.getSimpleName());

    /**
     * 身份名<br>
     * 该身份的唯一标识
     * 
     * @return
     */
    String getKey();

    /**
     * 连接的身份类型
     * 
     * @return
     */
    IdentityType getType();

}
