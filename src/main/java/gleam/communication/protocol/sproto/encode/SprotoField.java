package gleam.communication.protocol.sproto.encode;

import gleam.communication.protocol.sproto.define.SprotoFieldType;

/**
 * sproto编码时使用 协议中的单个参数
 * 
 * @author hdh
 * @time 2022年7月11日
 *
 */
class SprotoField implements Comparable<SprotoField> {
    /**
     * 参数序号<br>
     * 从0开始
     */
    private short tag;
    /**
     * 小参数<br>
     * 和tag共同占用描述段(2位)
     */
    private short value;
    /**
     * 该参数是否数组
     */
    private boolean array;
    /**
     * 参数类型
     */
    private SprotoFieldType type;
    /**
     * 大参数
     */
    private Object data;

    public short getTag() {
        return tag;
    }

    public void setTag(short tag) {
        this.tag = tag;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    public SprotoFieldType getType() {
        return type;
    }

    public void setType(SprotoFieldType type) {
        this.type = type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public Object getData() {
        return data;
    }

    @Override
    public int compareTo(SprotoField o) {
        return Integer.compare(tag, o.tag);
    }
}
