package gleam.communication.protocol.sproto.decode;

/**
 * 解析sproto协议时 单个参数的属性
 * 
 * @author hdh
 * @time 2022年7月11日
 *
 */
class SprotoFieldReader {

    private short tag;
    /**
     * 已-1<br>
     * 若<0 则为数据在数据段中 需要另外读取
     */
    private short value;
    /**
     * 在数据段的起始下标<br>
     * 读取数据长度后的下标
     */
    private int dataBeginIndex;

    private int dataLength;

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

    public int getDataBeginIndex() {
        return dataBeginIndex;
    }

    public void setDataBeginIndex(int dataBeginIndex) {
        this.dataBeginIndex = dataBeginIndex;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

}
