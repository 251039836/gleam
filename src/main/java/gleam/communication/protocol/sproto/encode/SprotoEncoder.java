package gleam.communication.protocol.sproto.encode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import gleam.communication.Protocol;
import gleam.communication.protocol.sproto.define.SprotoFieldType;
import io.netty.buffer.ByteBuf;

/**
 * sproto编码器<br>
 * 头部段 参数数量(2)<br>
 * 描述段 [tag(2)]<br>
 * 若值=0 则为tag++ 且数据在数据段中 <br>
 * 若值不为0 则16位中使用了1位标识为跳过n位还是短整数<br>
 * 奇数 则为下一个tag跳过(tagValue+1)/<br>
 * 偶数 则为tag++ 数据为tagValue/2-1 用于表示[0,32767]<br>
 * 数据段 [length(4)'该数据值占用的字节数',value'数据值'] <br>
 * 
 * 此类调用的地方应当只为自动生成<br>
 * 基于性能考虑省略掉大部分安全判断
 * 
 * @author hdh
 *
 */
public class SprotoEncoder {
    /**
     * 有实际内容的参数
     */
    private List<SprotoField> fields = new ArrayList<>();

    public void setValue(short tag, boolean value) {
//        if (value == false) {
//            // 默认值
//            return;
//        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setType(SprotoFieldType.BOOLEAN);
        field.setValue((short) (value ? 1 : 0));
        fields.add(field);
    }

    public void setValue(short tag, int value) {
//        if (value == 0) {
//            // 默认值
//            return;
//        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setType(SprotoFieldType.INTEGER);
        if (value >= 0 && value <= Short.MAX_VALUE) {
            field.setValue((short) value);
        } else {
            field.setData(value);
        }
        fields.add(field);
    }

    public void setValue(short tag, long value) {
//        if (value == 0) {
//            // 默认值
//            return;
//        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setType(SprotoFieldType.LONG);
        if (value >= 0 && value <= Short.MAX_VALUE) {
            field.setValue((short) value);
        } else {
            field.setData(value);
        }
        fields.add(field);
    }

    public void setValue(short tag, String value) {
        if (value == null) {
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setType(SprotoFieldType.STRING);
        field.setData(value);
        fields.add(field);
    }

    public void setValue(short tag, Protocol value) {
        if (value == null) {
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setType(SprotoFieldType.STRUCT);
        field.setData(value);
        fields.add(field);
    }

    public void setValue(short tag, byte[] value) {
        if (value == null) {
            // 默认值
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setType(SprotoFieldType.BINARY);
        field.setData(value);
        fields.add(field);
    }

    public void setBooleanValues(short tag, List<Boolean> values) {
        if (values == null) {
            // 默认值
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setArray(true);
        field.setType(SprotoFieldType.BOOLEAN);
        field.setData(values);
        fields.add(field);
    }

    public void setIntValues(short tag, List<Integer> values) {
        if (values == null) {
            // 默认值
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setArray(true);
        field.setType(SprotoFieldType.INTEGER);
        field.setData(values);
        fields.add(field);
    }

    public void setLongValues(short tag, List<Long> values) {
        if (values == null) {
            // 默认值
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setArray(true);
        field.setType(SprotoFieldType.LONG);
        field.setData(values);
        fields.add(field);
    }

    public void setStringValues(short tag, List<String> values) {
        if (values == null) {
            // 默认值
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setArray(true);
        field.setType(SprotoFieldType.STRING);
        field.setData(values);
        fields.add(field);
    }

    public void setProtocolValues(short tag, List<? extends Protocol> values) {
        if (values == null) {
            // 默认值
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setArray(true);
        field.setType(SprotoFieldType.STRUCT);
        field.setData(values);
        fields.add(field);
    }

    public void setBytesValues(short tag, List<byte[]> values) {
        if (values == null) {
            // 默认值
            return;
        }
        SprotoField field = new SprotoField();
        field.setTag(tag);
        field.setArray(true);
        field.setType(SprotoFieldType.BINARY);
        field.setData(values);
        fields.add(field);
    }

    /**
     * 将所有参数写进buffer
     * 
     * @param buffer
     */
    public void writeBuffer(ByteBuf buffer) {
        int fieldSize = fields.size();
        int fieldNumWriterIndex = buffer.writerIndex();
        buffer.writeShortLE(fieldSize);
        if (fieldSize <= 0) {
            return;
        }
        int skipCount = 0;
        int lastTag = -1;
        // 描述段
        for (SprotoField field : fields) {
            short tag = field.getTag();
            int skipTag = tag - lastTag - 1;
            if (skipTag > 0) {
                // 跳过了skipTag个标签
                // 额外增加2字节保存跳过信息
                // 1=1 2=3 3=5
                int writeSkipTag = 2 * skipTag - 1;
                buffer.writeShortLE(writeSkipTag);
                skipCount++;
            }
            lastTag = tag;
            Object data = field.getData();
            if (data != null) {
                // 数据要写到数据区
                buffer.writeShortLE(0);
                continue;
            }
            // 小整形 支持0~32767
            short value = field.getValue();
            int writeTag = (value + 1) * 2;
            buffer.writeShortLE(writeTag);
        }
        if (skipCount > 0) {
            buffer.setShortLE(fieldNumWriterIndex, fieldSize + skipCount);
        }

        // 数据段
        for (SprotoField field : fields) {
            Object data = field.getData();
            if (data == null) {
                continue;
            }
            boolean array = field.isArray();
            SprotoFieldType type = field.getType();
            if (!array) {
                writeObject(buffer, type, data);
            } else {
                writeArray(buffer, type, data);
            }
        }
    }

    private void writeObject(ByteBuf buffer, SprotoFieldType type, Object data) {
        if (type == SprotoFieldType.INTEGER) {
            buffer.writeIntLE(4);
            buffer.writeIntLE((int) data);
        } else if (type == SprotoFieldType.LONG) {
            buffer.writeIntLE(8);
            buffer.writeLongLE((long) data);
        } else if (type == SprotoFieldType.STRING) {
            String str = (String) data;
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            buffer.writeIntLE(bytes.length);
            buffer.writeBytes(bytes);
        } else if (type == SprotoFieldType.STRUCT) {
            Protocol protocol = (Protocol) data;
            int beginWriterIndex = buffer.writerIndex();
            buffer.writeIntLE(0);
            protocol.encode(buffer);
            int endWriterIndex = buffer.writerIndex();
            int protocolLength = endWriterIndex - beginWriterIndex - 4;
            buffer.setIntLE(beginWriterIndex, protocolLength);
        } else if (type == SprotoFieldType.BINARY) {
            byte[] bytes = (byte[]) data;
            buffer.writeIntLE(bytes.length);
            buffer.writeBytes(bytes);
        } else {
            throw new RuntimeException("write object error.unknown fieldType[" + type.name() + "]");
        }
    }

    @SuppressWarnings("unchecked")
    private void writeArray(ByteBuf buffer, SprotoFieldType type, Object data) {
        if (type == SprotoFieldType.INTEGER) {
            List<Integer> intList = (List<Integer>) data;
            // 直接写入后续长度
            buffer.writeIntLE(intList.size() * 4 + 1);
            buffer.writeByte(4);
            for (int value : intList) {
                buffer.writeIntLE(value);
            }
        } else if (type == SprotoFieldType.LONG) {
            List<Long> longList = (List<Long>) data;
            // 直接写入后续长度
            buffer.writeIntLE(longList.size() * 8 + 1);
            buffer.writeByte(8);
            for (long value : longList) {
                buffer.writeLongLE(value);
            }
        } else if (type == SprotoFieldType.STRING) {
            // 写完再计算长度
            int dataLengthWriterIndex = buffer.writerIndex();
            buffer.writeIntLE(0);
            List<String> strList = (List<String>) data;
            for (String str : strList) {
                if (str == null) {
                    buffer.writeIntLE(0);
                } else {
                    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
                    buffer.writeIntLE(bytes.length);
                    buffer.writeBytes(bytes);
                }
            }
            int listEndWriterIndex = buffer.writerIndex();
            int bufferLength = listEndWriterIndex - dataLengthWriterIndex - 4;
            buffer.setIntLE(dataLengthWriterIndex, bufferLength);
        } else if (type == SprotoFieldType.STRUCT) {
            // 写完再计算长度
            int dataLengthWriterIndex = buffer.writerIndex();
            buffer.writeIntLE(0);
            List<Protocol> protocolList = (List<Protocol>) data;
            for (Protocol protocol : protocolList) {
                int beginWriterIndex = buffer.writerIndex();
                buffer.writeIntLE(0);
                protocol.encode(buffer);
                int endWriterIndex = buffer.writerIndex();
                int protocolLength = endWriterIndex - beginWriterIndex - 4;
                buffer.setIntLE(beginWriterIndex, protocolLength);
            }
            int listEndWriterIndex = buffer.writerIndex();
            int bufferLength = listEndWriterIndex - dataLengthWriterIndex - 4;
            buffer.setIntLE(dataLengthWriterIndex, bufferLength);
        } else if (type == SprotoFieldType.BINARY) {
            // 写完再计算长度
            int dataLengthWriterIndex = buffer.writerIndex();
            buffer.writeIntLE(0);
            List<byte[]> bytesList = (List<byte[]>) data;
            for (byte[] bytes : bytesList) {
                if (bytes == null) {
                    buffer.writeIntLE(0);
                } else {
                    buffer.writeIntLE(bytes.length);
                    buffer.writeBytes(bytes);
                }
            }
            int listEndWriterIndex = buffer.writerIndex();
            int bufferLength = listEndWriterIndex - dataLengthWriterIndex - 4;
            buffer.setIntLE(dataLengthWriterIndex, bufferLength);
        } else if (type == SprotoFieldType.BOOLEAN) {
            // 直接写入长度
            List<Boolean> booleanList = (List<Boolean>) data;
            buffer.writeIntLE(booleanList.size());
            for (boolean value : booleanList) {
                if (value) {
                    buffer.writeByte(2);
                } else {
                    buffer.writeByte(1);
                }
            }
        } else {
            throw new RuntimeException("write object error.unknown fieldType[" + type.name() + "]");
        }
    }

}
