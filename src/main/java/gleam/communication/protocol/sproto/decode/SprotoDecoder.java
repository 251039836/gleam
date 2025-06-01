package gleam.communication.protocol.sproto.decode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.communication.Protocol;
import io.netty.buffer.ByteBuf;

/**
 * sproto协议解码器
 * 
 * @author hdh
 * @time 2022年7月11日
 *
 */
public class SprotoDecoder {

	private final static Logger logger = LoggerFactory.getLogger(SprotoDecoder.class);

	private ByteBuf buffer;

	private List<SprotoFieldReader> fields = new ArrayList<>();

	public SprotoDecoder() {
	}

	public void readBuffer(ByteBuf buffer) {
		int fieldNum = buffer.readShortLE();
		if (fieldNum <= 0) {
			return;
		}
		this.buffer = buffer;
		short tag = 0;
		List<SprotoFieldReader> dataList = new ArrayList<>(fieldNum);
		// 描述段
		for (int i = 0; i < fieldNum; i++) {
			short tagValue = buffer.readShortLE();
			if (tagValue % 2 == 1) {
				// 奇数时为跳过n个标签
				// 1=1 3=2 5=3 7=4
				int skipTag = (tagValue + 1) / 2;
				tag += skipTag;
				continue;
			}
			SprotoFieldReader field = new SprotoFieldReader();
			field.setTag(tag);
			fields.add(field);
			tag++;
			if (tagValue == 0) {
				// 0 时 数据在数据区
				dataList.add(field);
				continue;
			}
			// 偶数时 为直接保存数值
			short value = (short) (tagValue / 2 - 1);
			field.setValue(value);
		}
		// 数据段
		for (int i = 0; i < dataList.size(); i++) {
			SprotoFieldReader fieldReader = dataList.get(i);
			int dataLength = buffer.readIntLE();
			int readerIndex = buffer.readerIndex();

			fieldReader.setDataBeginIndex(readerIndex);
			fieldReader.setDataLength(dataLength);
			int nextReaderIndex = readerIndex + dataLength;
			buffer.readerIndex(nextReaderIndex);
		}
	}

	public ByteBuf getBuffer() {
		return buffer;
	}

	public void setBuffer(ByteBuf buffer) {
		this.buffer = buffer;
	}

	public List<SprotoFieldReader> getFields() {
		return fields;
	}

	public void setFields(List<SprotoFieldReader> fields) {
		this.fields = fields;
	}

	private SprotoFieldReader getFieldReader(short tag) {
		for (SprotoFieldReader field : fields) {
			if (field.getTag() == tag) {
				return field;
			}
		}
		return null;
	}

	public boolean getBooleanValue(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return false;
		}
		short value = fieldReader.getValue();
		return value > 0;
	}

	public int getIntValue(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return 0;
		}
		int dataLength = fieldReader.getDataLength();
		if (dataLength <= 0) {
			return fieldReader.getValue();
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int data = 0;
		if (dataLength == 4) {
			data = buffer.getIntLE(dataBeginIndex);
		} else if (dataLength == 8) {
			data = (int) buffer.getLongLE(dataBeginIndex);
		} else {
			logger.warn("read tag[{}] int error.dataLength:{}", dataLength);
		}
		return data;
	}

	public long getLongValue(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return 0;
		}
		int dataLength = fieldReader.getDataLength();
		if (dataLength <= 0) {
			return fieldReader.getValue();
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		long data = 0;
		if (dataLength == 4) {
			data = buffer.getIntLE(dataBeginIndex);
		} else if (dataLength == 8) {
			data = buffer.getLongLE(dataBeginIndex);
		} else {
			logger.warn("read tag[{}] long error.dataLength:{}", dataLength);
		}
		return data;
	}

	public String getStringValue(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return null;
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int dataLength = fieldReader.getDataLength();
		byte[] data = new byte[dataLength];
		buffer.getBytes(dataBeginIndex, data);
		String str = new String(data, StandardCharsets.UTF_8);
		return str;
	}

	public <T extends Protocol> T getProtocolValue(short tag, Class<T> clazz) throws Exception {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return null;
		}
		T protocol = clazz.getConstructor().newInstance();
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int readerIndex = buffer.readerIndex();
		buffer.readerIndex(dataBeginIndex);
		protocol.decode(buffer);
		buffer.readerIndex(readerIndex);
		return protocol;
	}

	public byte[] getBytesValue(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return null;
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int dataLength = fieldReader.getDataLength();
		byte[] data = new byte[dataLength];
		buffer.getBytes(dataBeginIndex, data);
		return data;
	}

	public List<Boolean> getBooleanValues(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return Collections.emptyList();
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int dataLength = fieldReader.getDataLength();
		byte[] tmpBytes = new byte[dataLength];
		buffer.getBytes(dataBeginIndex, tmpBytes);
		List<Boolean> result = new ArrayList<>(dataLength);
		for (byte tmpByte : tmpBytes) {
			if (tmpByte > 1) {
				result.add(true);
			} else {
				result.add(false);
			}
		}
		return result;
	}

	public List<Integer> getIntValues(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return Collections.emptyList();
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int dataLength = fieldReader.getDataLength();
		byte numLength = buffer.getByte(dataBeginIndex);
		dataBeginIndex++;
		List<Integer> result = null;
		if (numLength == 4) {
			int resultSize = (dataLength - 1) / 4;
			result = new ArrayList<>(resultSize);
			for (int i = 0; i < resultSize; i++) {
				int num = buffer.getIntLE(dataBeginIndex);
				result.add(num);
				dataBeginIndex += 4;
			}
		} else if (numLength == 8) {
			int resultSize = (dataLength - 1) / 8;
			result = new ArrayList<>(resultSize);
			for (int i = 0; i < resultSize; i++) {
				int num = (int) buffer.getLongLE(dataBeginIndex);
				result.add(num);
				dataBeginIndex += 8;
			}
		} else {
			logger.warn("read tag[{}] intList error.dataLength:{} numLength", dataLength, numLength);
		}
		return result;
	}

	public List<Long> getLongValues(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return Collections.emptyList();
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int dataLength = fieldReader.getDataLength();
		byte numLength = buffer.getByte(dataBeginIndex);
		dataBeginIndex++;
		List<Long> result = null;
		if (numLength == 8) {
			int resultSize = (dataLength - 1) / 8;
			result = new ArrayList<>(resultSize);
			for (int i = 0; i < resultSize; i++) {
				long num = buffer.getLongLE(dataBeginIndex);
				result.add(num);
				dataBeginIndex += 8;
			}
		} else if (numLength == 4) {
			int resultSize = (dataLength - 1) / 4;
			result = new ArrayList<>(resultSize);
			for (int i = 0; i < resultSize; i++) {
				long num = buffer.getIntLE(dataBeginIndex);
				result.add(num);
				dataBeginIndex += 4;
			}
		} else {
			logger.warn("read tag[{}] longList error.dataLength:{} numLength", dataLength, numLength);
		}
		return result;
	}

	public List<String> getStringValues(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return Collections.emptyList();
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int dataLength = fieldReader.getDataLength();
		List<String> result = new ArrayList<>();
		while (dataLength > 0) {
			int bytesLength = buffer.getIntLE(dataBeginIndex);
			dataBeginIndex += 4;
			byte[] tmpBytes = new byte[bytesLength];
			buffer.getBytes(dataBeginIndex, tmpBytes);
			dataBeginIndex += bytesLength;
			String str = new String(tmpBytes, StandardCharsets.UTF_8);
			result.add(str);
			dataLength = dataLength - 4 - bytesLength;
		}
		return result;
	}

	public <T extends Protocol> List<T> getProtocolValues(short tag, Class<T> clazz) throws Exception {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return Collections.emptyList();
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int dataLength = fieldReader.getDataLength();
		int readerIndex = buffer.readerIndex();
		buffer.readerIndex(dataBeginIndex);
		List<T> result = new ArrayList<>();
		while (dataLength > 0) {
			int bytesLength = buffer.readIntLE();
			T tmpProtocol = clazz.getConstructor().newInstance();
			tmpProtocol.decode(buffer);
			result.add(tmpProtocol);
			dataLength = dataLength - 4 - bytesLength;
		}
		buffer.readerIndex(readerIndex);
		return result;
	}

	public List<byte[]> getBytesValues(short tag) {
		SprotoFieldReader fieldReader = getFieldReader(tag);
		if (fieldReader == null) {
			return Collections.emptyList();
		}
		int dataBeginIndex = fieldReader.getDataBeginIndex();
		int dataLength = fieldReader.getDataLength();
		List<byte[]> result = new ArrayList<>();
		while (dataLength > 0) {
			int bytesLength = buffer.getIntLE(dataBeginIndex);
			dataBeginIndex += 4;
			byte[] tmpBytes = new byte[bytesLength];
			buffer.getBytes(dataBeginIndex, tmpBytes);
			dataBeginIndex += bytesLength;
			result.add(tmpBytes);
			dataLength = dataLength - 4 - bytesLength;
		}
		return result;
	}

}
