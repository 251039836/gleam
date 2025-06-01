package gleam.communication.protocol.sproto.define;

public enum SprotoFieldType {

    BOOLEAN,
    /**
     * 
     */
    INTEGER,
    /**
     * 
     */
    LONG,
    /**
     *
     */
    STRING,
    /**
     * 结构体<br>
     * 其他协议
     */
    STRUCT,
    /**
     * byte[]<br>
     * 虽然是数组 但当成一种普通的类型直接处理
     */
    BINARY

    ;

}
