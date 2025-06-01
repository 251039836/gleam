package gleam.util.guid;

/**
 * 全局唯一id相关定义
 * 
 * @author hdh
 *
 */
public class GuidDefines {
    /**
     * 服务器id所占的位数<br>
     * 最大值131071
     */
    public final static int SERVER_ID_LENGTH = 17;
    /**
     * guid类型所占的位数<br>
     * 最大值15
     */
    public final static int GUID_TYPE_LENGTH = 4;
    /**
     * 计数器所占位数<br>
     * 最大值2^42-1<br>
     * 4 398 046 511 103
     */
    public final static int COUNTER_LENGTH = 64 - 1 - SERVER_ID_LENGTH - GUID_TYPE_LENGTH;

    /**
     * 服务器id在guid中的偏移量
     */
    public final static int SERVER_ID_OFFSET = 0;
    /**
     * guid类型在guid中的偏移值
     */
    public final static int GUID_TYPE_OFFSET = SERVER_ID_LENGTH;
    /**
     * 计数器在guid中的偏移量
     */
    public final static int COUNTER_OFFSET = GUID_TYPE_LENGTH + SERVER_ID_LENGTH;
    /**
     * guid类型上限
     */
    public final static int GUID_TYPE_UPPER_BOUND = (1 << GUID_TYPE_LENGTH) - 1;
    /**
     * 服务器id上限
     */
    public final static int SERVER_ID_UPPER_BOUND = (1 << SERVER_ID_LENGTH) - 1;
    /**
     * 计数器上限<br>
     * 该类型的全局唯一id数量上限
     */
    public final static long COUNTER_UPPER_BOUND = (1l << COUNTER_LENGTH) - 1;
}
