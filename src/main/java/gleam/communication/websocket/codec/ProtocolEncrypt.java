package gleam.communication.websocket.codec;

import io.netty.buffer.ByteBuf;
import io.netty.util.AttributeKey;

/**
 * 协议加密
 * 
 * @author hdh
 *
 */
public class ProtocolEncrypt {

    public static final AttributeKey<ProtocolEncrypt> ATTR_KEY = AttributeKey.valueOf(ProtocolEncrypt.class.getSimpleName());
    /**
     * 加密字节数字<br>
     */
    private byte[] encryptBytes;
    /**
     * 加密长度
     */
    private int length;

    public ProtocolEncrypt(int key, int length) {
        this.encryptBytes = buildEncryptBytes(key);
        this.length = length;
    }

    private byte[] buildEncryptBytes(int key) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; ++i) {
            result[i] = (byte) ((key >> (i << 3)) & 0xff);
        }
        return result;
    }

    public void decode(ByteBuf in) {
        int remainLength = in.readableBytes();
        if (remainLength <= 0) {
            return;
        }
        // 记录下标
        int readerIndex = in.readerIndex();
        int changeLength = Math.min(length, remainLength);
        byte[] changeData = new byte[changeLength];
        in.getBytes(readerIndex, changeData);
        for (int i = 0; i < changeLength; i++) {
            byte b = changeData[i];
            b ^= encryptBytes[i % 4];
            changeData[i] = b;
        }
        in.setBytes(readerIndex, changeData);
    }

    protected byte[] getEncryptBytes() {
        return encryptBytes;
    }

    protected int getLength() {
        return length;
    }

    protected void setEncryptBytes(byte[] encryptBytes) {
        this.encryptBytes = encryptBytes;
    }

    protected void setLength(int length) {
        this.length = length;
    }

}
