package gleam.communication.authenticate;

/**
 * 身份类型
 * 
 * @author hdh
 *
 */
public enum IdentityType {
    /** 玩家客户端 */
    CLIENT(1, false),
    /** 游戏服 */
    LOGIC(2, true),
    /** 登录服(网关) */
    GATE(3, true),
    /** 跨服 */
    CROSS(4, true),

    ;

    public static IdentityType get(int type) {
        IdentityType[] identityTypes = IdentityType.values();
        for (IdentityType identityType : identityTypes) {
            if (identityType.getType() == type) {
                return identityType;
            }
        }
        return null;
    }

    private final int type;

    /**
     * 是否内网连接
     */
    private final boolean inner;

    private IdentityType(int type, boolean inner) {
        this.type = type;
        this.inner = inner;
    }

    public int getType() {
        return type;
    }

    public boolean isInner() {
        return inner;
    }

}
