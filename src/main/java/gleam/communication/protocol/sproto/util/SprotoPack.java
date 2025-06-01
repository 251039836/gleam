package gleam.communication.protocol.sproto.util;

import java.util.Arrays;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * sproto打包工具类<br>
 * 输入和还原数据时 都是以8个字节为一组进行操作<br>
 * 将每8个字节转为 {首字节(每一bit倒序对应原8字节哪位不为0) 原8字节中的非0字节}<br>
 * unpacked (hex): 08 00 00 00 03 00 02 00 19 00 00 00 aa 01 00 00<br>
 * packed (hex): (0b01010001)51 08 03 02 (0b00110001)31 19 aa 01<br>
 * 当连续8个字节都不为0时 <br>
 * 首字节的下一个字节 标明后续有多少1+(1~256)组(8字节) >=6个字节非0<br>
 * (sproto和capnproto不一样之处 sproto不处理都为0的情况)<br>
 * unpacked (hex): 8a (x 30 bytes)<br>
 * packed (hex): ff 03 8a (x 30 bytes) 00 00<br>
 * 
 * 
 * 在非sproto场景中并不适用 解压可能会导致在结尾多出多个值为0的字节 将长度补足到8的倍数<br>
 * <br>
 * {@link https://github.com/cloudwu/sproto}<br>
 * {@link https://blog.codingnow.com/2014/07/ejoyproto.html}<br>
 * {@link https://capnproto.org/encoding.html#packing}<br>
 * 
 * @author hdh
 * @time 2022年7月7日
 *
 */
public class SprotoPack {

    private final static byte FULL_SEGMENT_HEADER = (byte) 0xff;

    /**
     * 打包字节流 以sprotoPack的方式进行打包压缩
     * 
     * @param src
     * @return
     */
    public static byte[] pack(byte[] src) {
        int srcLength = src.length;
        if (srcLength <= 0) {
            return new byte[0];
        }
        byte[] packed = new byte[srcLength + 2];
        int fullSegmentSizeIndex = -1;
        byte fullSegmentSize = 0;
        int writeIndex = 0;
        int notZero = 0;
        byte header = 0;
        int curSegmentLength = 0;
        for (int i = 0; i < srcLength; i += 8) {// 每8个字节一组
            curSegmentLength = Math.min(8, srcLength - i);// 本段可读的长度
            notZero = 0;
            header = 0;
            for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                byte value = src[i + j];
                if (value != 0) {
                    notZero++;
                    header |= (byte) (1 << j);
                }
            }
            if (fullSegmentSizeIndex < 0) {
                // 之前一段不是长数据
                if (header == FULL_SEGMENT_HEADER) {
                    // 本段是长数据 则在本段头部之后插入后续长数据段大小
                    // [0]0xff [1]后续长数据段大小(不含该端) [2~9]本段数据
                    packed[writeIndex++] = header;
                    // 插入后续长数据段大小
                    fullSegmentSizeIndex = writeIndex++;
                    packed[fullSegmentSizeIndex] = fullSegmentSize;
                    // 原8位后移1位
                    for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                        packed[writeIndex++] = src[i + j];
                    }
                } else {
                    // 本段不是长数据 正常写入头部
                    packed[writeIndex++] = header;
                    for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                        byte value = src[i + j];
                        if (value != 0) {
                            packed[writeIndex++] = value;
                        }
                    }
                }
            } else {
                // 当前在长数据段之中
                if (notZero < 6) {
                    // 本段已结束长数据段
                    // 按正常的压缩方式写入
                    packed[writeIndex++] = header;
                    for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                        byte value = src[i + j];
                        if (value != 0) {
                            packed[writeIndex++] = value;
                        }
                    }
                    fullSegmentSizeIndex = -1;
                    fullSegmentSize = 0;
                } else {
                    fullSegmentSize++;
                    if (fullSegmentSize != 0) {
                        // 1+256以内
                        // 次段开始 >=678个的都视为长数据段
                        // 不再需要填充头部 直接写入数据
                        for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                            packed[writeIndex++] = src[i + j];
                        }
                        packed[fullSegmentSizeIndex] = fullSegmentSize;
                    } else {
                        // 1+256 超长段已结束
                        if (header == FULL_SEGMENT_HEADER) {
                            // 重新开始长段
                            packed[writeIndex++] = header;
                            // 插入后续长数据段大小
                            fullSegmentSizeIndex = writeIndex++;
                            packed[fullSegmentSizeIndex] = fullSegmentSize;
                            // 原8位后移1位
                            for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                                packed[writeIndex++] = src[i + j];
                            }
                        } else {
                            // 结束长段 开始普通段
                            packed[writeIndex++] = header;
                            for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                                byte value = src[i + j];
                                if (value != 0) {
                                    packed[writeIndex++] = value;
                                }
                            }
                            fullSegmentSizeIndex = -1;
                            fullSegmentSize = 0;
                        }
                    }
                }
            }
        }
        byte[] result = Arrays.copyOf(packed, writeIndex);
        return result;
    }

    /**
     * 压缩
     * 
     * @param buffer
     * @param beginIndex 从此下标开始压缩
     * @return
     */
    public static void pack(ByteBuf buffer, int beginIndex) {
        int oldReaderIndex = buffer.readerIndex();
        buffer.readerIndex(beginIndex);
        int srcLength = buffer.readableBytes();
        buffer.readerIndex(oldReaderIndex);
        if (srcLength <= 0) {
            // 没有需要压缩的
            return;
        }
        byte[] src = new byte[srcLength];
        buffer.getBytes(beginIndex, src);
        byte[] packed = new byte[srcLength + 2];
        int fullSegmentSizeIndex = -1;
        byte fullSegmentSize = 0;
        int packedWriteIndex = 0;//
        int notZero = 0;
        byte header = 0;
        int curSegmentLength = 0;
        for (int i = 0; i < srcLength; i += 8) {// 每8个字节一组
            curSegmentLength = Math.min(8, srcLength - i);// 本段可读的长度
            notZero = 0;
            header = 0;
            for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                byte value = src[i + j];
                if (value != 0) {
                    notZero++;
                    header |= (byte) (1 << j);
                }
            }
            if (fullSegmentSizeIndex < 0) {
                // 之前一段不是长数据
                if (header == FULL_SEGMENT_HEADER) {
                    // 本段是长数据 则在本段头部之后插入后续长数据段大小
                    // [0]0xff [1]后续长数据段大小(不含该端) [2~9]本段数据
                    packed[packedWriteIndex++] = header;
                    // 插入后续长数据段大小
                    fullSegmentSizeIndex = packedWriteIndex++;
                    packed[fullSegmentSizeIndex] = fullSegmentSize;
                    // 原8位后移1位
                    for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                        packed[packedWriteIndex++] = src[i + j];
                    }
                } else {
                    // 本段不是长数据 正常写入头部
                    packed[packedWriteIndex++] = header;
                    for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                        byte value = src[i + j];
                        if (value != 0) {
                            packed[packedWriteIndex++] = value;
                        }
                    }
                }
            } else {
                // 当前在长数据段之中
                if (notZero < 6) {
                    // 本段已结束长数据段
                    // 按正常的压缩方式写入
                    packed[packedWriteIndex++] = header;
                    for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                        byte value = src[i + j];
                        if (value != 0) {
                            packed[packedWriteIndex++] = value;
                        }
                    }
                    fullSegmentSizeIndex = -1;
                    fullSegmentSize = 0;
                } else {
                    fullSegmentSize++;
                    if (fullSegmentSize != 0) {
                        // 1+256以内
                        // 次段开始 >=678个的都视为长数据段
                        // 不再需要填充头部 直接写入数据
                        for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                            packed[packedWriteIndex++] = src[i + j];
                        }
                        packed[fullSegmentSizeIndex] = fullSegmentSize;
                    } else {
                        // 1+256 超长段已结束
                        if (header == FULL_SEGMENT_HEADER) {
                            // 重新开始长段
                            packed[packedWriteIndex++] = header;
                            // 插入后续长数据段大小
                            fullSegmentSizeIndex = packedWriteIndex++;
                            packed[fullSegmentSizeIndex] = fullSegmentSize;
                            // 原8位后移1位
                            for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                                packed[packedWriteIndex++] = src[i + j];
                            }
                        } else {
                            // 结束长段 开始普通段
                            packed[packedWriteIndex++] = header;
                            for (int j = 0; j < 8 && j < curSegmentLength; j++) {
                                byte value = src[i + j];
                                if (value != 0) {
                                    packed[packedWriteIndex++] = value;
                                }
                            }
                            fullSegmentSizeIndex = -1;
                            fullSegmentSize = 0;
                        }
                    }
                }
            }
        }
        // 从起始下标开始写入
        buffer.writerIndex(beginIndex);
        buffer.writeBytes(packed, 0, packedWriteIndex);
    }

    /**
     * 解压
     * 
     * @param src
     * @return
     */
    public static byte[] unpack(byte[] src) {
        int packedLength = src.length;
        if (packedLength <= 0) {
            return new byte[0];
        }
        byte[] tmpBuffer = new byte[packedLength * 2];
        int readIndex = 0;
        int writeIndex = 0;
        while (readIndex < packedLength) {
            byte header = src[readIndex++];
            if (header == FULL_SEGMENT_HEADER) {
                // 1+n组
                int fullSegmentSize = Byte.toUnsignedInt(src[readIndex++]) + 1;
                int fullSegmentLength = fullSegmentSize * 8;
                if (readIndex + fullSegmentLength > packedLength) {
                    fullSegmentLength = packedLength - readIndex;
                }
                tmpBuffer = expand(tmpBuffer, writeIndex, fullSegmentLength);
                System.arraycopy(src, readIndex, tmpBuffer, writeIndex, fullSegmentLength);
                readIndex = readIndex + fullSegmentLength;
                writeIndex = writeIndex + fullSegmentLength;
            } else {
                // 不满的情况
                tmpBuffer = expand(tmpBuffer, writeIndex, 8);
                for (int i = 0; i < 8 && readIndex < packedLength; i++) {
                    int nz = (header >> i) & 1;
                    if (nz == 1) {
                        tmpBuffer[writeIndex] = src[readIndex++];
                    }
                    writeIndex++;
                }
            }
        }
        // 长度补足到8的倍数
        int resultLength = ((writeIndex + 7) / 8) * 8;
        byte[] result = Arrays.copyOf(tmpBuffer, resultLength);
        return result;
    }

    /**
     * 解压
     * 
     * @param buffer
     * @return 原buffer未必可以扩展 可能返回新buffer
     */
    public static ByteBuf unpack(ByteBuf buffer) {
        int packedLength = buffer.readableBytes();
        if (packedLength <= 0) {
            return buffer;
        }
        byte[] src = new byte[packedLength];
        buffer.readBytes(src);
        ByteBuf unpackBuffer = PooledByteBufAllocator.DEFAULT.buffer(packedLength * 2);
        int readIndex = 0;
        while (readIndex < packedLength) {
            byte header = src[readIndex++];
            if (header == FULL_SEGMENT_HEADER) {
                // 满载 直接复制
                // 1+n组
                int fullSegmentSize = Byte.toUnsignedInt(src[readIndex++]) + 1;
                int fullSegmentLength = fullSegmentSize * 8;
                if (readIndex + fullSegmentLength > packedLength) {
                    fullSegmentLength = packedLength - readIndex;
                }
                unpackBuffer.writeBytes(src, readIndex, fullSegmentLength);
                readIndex = readIndex + fullSegmentLength;
            } else {
                // 不满的情况 8位标识后续的8个字节哪个有内容
                for (int i = 0; i < 8 && readIndex < packedLength; i++) {
                    int nz = (header >> i) & 1;
                    if (nz == 1) {
                        unpackBuffer.writeByte(src[readIndex++]);
                    } else {
                        unpackBuffer.writeByte(0);
                    }
                }
            }
        }
        return unpackBuffer;
    }

    private static byte[] expand(byte[] buffer, int writeIndex, int writeSize) {
        int bufferLength = buffer.length;
        int needLength = writeIndex + writeSize + 1;
        if (bufferLength >= needLength) {
            return buffer;
        }
        while (bufferLength < needLength) {
            bufferLength *= 2;
        }
        byte[] newBuffer = Arrays.copyOf(buffer, bufferLength);
        return newBuffer;
    }
}
