package gleam.util.buffer;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import gleam.exception.BufferReadDataException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;

/**
 * buffer的封装类默认实现<br>
 * 默认使用netty的 UnpooledByteBuf.heapBuffer()
 * 
 * @author hdh
 *
 */
public class DefaultBufferWrapper implements BufferWrapper {

	private final static Charset Default_Charset = StandardCharsets.UTF_8;

	private final ByteBuf buffer;

	public DefaultBufferWrapper() {
		this.buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer();
	}

	public DefaultBufferWrapper(byte[] bytes) {
		this.buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(bytes.length);
		buffer.writeBytes(bytes);
	}

	public DefaultBufferWrapper(ByteBuf buffer) {
		this.buffer = buffer;
	}

	public DefaultBufferWrapper(int initialCapacity) {
		this.buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(initialCapacity);
	}

	/// ------------------ 读-------------------------///
	@Override
	public void clear() {
		buffer.clear();
	}

	public ByteBuf getBuffer() {
		return buffer;
	}

	/**
	 * 把缓冲器中所有未读的byte取出
	 * 
	 * @return
	 */
	@Override
	public byte[] getData() {
		int length = buffer.readableBytes();
		byte[] data = new byte[length];
		buffer.readBytes(data);
		return data;
	}

	/// ------------------ 写-------------------------///
	/// ------------------ 读-------------------------/// @Override
	@Override
	public boolean readBoolean() {
		return buffer.readBoolean();
	}

	@Override
	public byte readByte() {
		return buffer.readByte();
	}

	@Override
	public byte[] readBytes(int size) {
		byte[] bytes = new byte[size];
		buffer.readBytes(bytes);
		return bytes;
	}

	@Override
	public char readChar() {
		return buffer.readChar();
	}

	@Override
	public <T extends ByteSerialize> T readData(Class<T> clazz) {
		try {
			T value = clazz.getConstructor().newInstance();
			value.readBuffer(this);
			return value;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new BufferReadDataException("buffer read clazz[" + clazz.getName() + "] error.", e);
		}
	}

	@Override
	public <T extends ByteSerialize> Collection<T> readDatas(Class<T> clazz) {
		short size = buffer.readShort();
		List<T> list = new ArrayList<>();
		for (short s = 0; s < size; s++) {
			T value = readData(clazz);
			list.add(value);
		}
		return list;
	}

	@Override
	public <T> Collection<T> readDatas(DataBufferReader<T> reader) {
		short size = buffer.readShort();
		List<T> list = new ArrayList<>();
		for (short s = 0; s < size; s++) {
			T value = reader.readData(this);
			list.add(value);
		}
		return list;
	}

	@Override
	public double readDouble() {
		return buffer.readDouble();
	}

	@Override
	public int readerIndex() {
		int readerIndex = buffer.readerIndex();
		return readerIndex;
	}

	@Override
	public BufferWrapper readerIndex(int readerIndex) {
		buffer.readerIndex(readerIndex);
		return this;
	}

	@Override
	public float readFloat() {
		return buffer.readFloat();
	}

	@Override
	public Map<Integer, Byte> readIBMap() {
		short size = buffer.readShort();
		Map<Integer, Byte> result = new HashMap<>();
		if (size <= 0) {
			return result;
		}
		for (short s = 0; s < size; s++) {
			int key = buffer.readInt();
			byte value = buffer.readByte();
			result.put(key, value);
		}
		return result;
	}

	@Override
	public Map<Byte, Long> readBLMap() {
		short size = buffer.readShort();
		Map<Byte, Long> result = new HashMap<>();
		if (size <= 0) {
			return result;
		}
		for (short s = 0; s < size; s++) {
			byte key = buffer.readByte();
			long value = buffer.readLong();
			result.put(key, value);
		}
		return result;
	}

	@Override
	public Map<Integer, Integer> readIIMap() {
		short size = buffer.readShort();
		Map<Integer, Integer> result = new HashMap<>();
		if (size <= 0) {
			return result;
		}
		for (short s = 0; s < size; s++) {
			int key = buffer.readInt();
			int value = buffer.readInt();
			result.put(key, value);
		}
		return result;
	}

	@Override
	public Map<Integer, Long> readILMap() {
		short size = buffer.readShort();
		Map<Integer, Long> result = new HashMap<>();
		if (size <= 0) {
			return result;
		}
		for (short s = 0; s < size; s++) {
			int key = buffer.readInt();
			long value = buffer.readLong();
			result.put(key, value);
		}
		return result;
	}

	@Override
	public int readInt() {
		return buffer.readInt();
	}

	@Override
	public Collection<Integer> readInts() {
		short size = buffer.readShort();
		List<Integer> list = new ArrayList<>();
		for (short s = 0; s < size; s++) {
			int value = buffer.readInt();
			list.add(value);
		}
		return list;
	}

	@Override
	public Map<Long, Integer> readLIMap() {
		short size = buffer.readShort();
		Map<Long, Integer> result = new HashMap<>();
		if (size <= 0) {
			return result;
		}
		for (short s = 0; s < size; s++) {
			long key = buffer.readLong();
			int value = buffer.readInt();
			result.put(key, value);
		}
		return result;
	}

	@Override
	public Map<Long, Long> readLLMap() {
		short size = buffer.readShort();
		Map<Long, Long> result = new HashMap<>();
		if (size <= 0) {
			return result;
		}
		for (short s = 0; s < size; s++) {
			long key = buffer.readLong();
			long value = buffer.readLong();
			result.put(key, value);
		}
		return result;
	}

	@Override
	public long readLong() {
		return buffer.readLong();
	}

	@Override
	public Collection<Long> readLongs() {
		short size = buffer.readShort();
		List<Long> list = new ArrayList<>();
		for (short s = 0; s < size; s++) {
			long value = buffer.readLong();
			list.add(value);
		}
		return list;
	}

	@Override
	public short readShort() {
		return buffer.readShort();
	}

	@Override
	public String readString() {
		short length = buffer.readShort();
		if (length <= 0) {
			return StringUtils.EMPTY;
		}
		byte[] bytes = new byte[length];
		buffer.readBytes(bytes);
		String value = new String(bytes, Default_Charset);
		return value;
	}

	@Override
	public String readString(Charset charset) {
		short length = buffer.readShort();
		if (length <= 0) {
			return null;
		}
		byte[] bytes = new byte[length];
		buffer.readBytes(bytes);
		String value = new String(bytes, charset);
		return value;
	}

	@Override
	public Collection<String> readStrings() {
		short size = buffer.readShort();
		List<String> list = new ArrayList<>();
		for (short s = 0; s < size; s++) {
			String value = readString();
			list.add(value);
		}
		return list;
	}

	/// ------------------ 写-------------------------///
	@Override
	public BufferWrapper writeBoolean(boolean value) {
		buffer.writeBoolean(value);
		return this;
	}

	@Override
	public BufferWrapper writeByte(int value) {
		buffer.writeByte(value);
		return this;
	}

	@Override
	public BufferWrapper writeBytes(byte[] bytes) {
		buffer.writeBytes(bytes);
		return this;

	}

	@Override
	public BufferWrapper writeChar(char value) {
		buffer.writeChar(value);
		return this;
	}

	@Override
	public <T extends ByteSerialize> BufferWrapper writeData(T value) {
		value.writeBuffer(this);
		return this;
	}

	@Override
	public <T extends ByteSerialize> BufferWrapper writeDatas(Collection<T> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (T value : values) {
			writeData(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public <T> BufferWrapper writeDatas(Collection<T> values, DataBufferWriter<T> writer) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (T value : values) {
			writer.writeData(value, this);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeDouble(double value) {
		buffer.writeDouble(value);
		return this;
	}

	@Override
	public BufferWrapper writeFloat(float value) {
		buffer.writeFloat(value);
		return this;
	}

	@Override
	public BufferWrapper writeBLMap(Map<Byte, Long> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (Entry<Byte, Long> entry : values.entrySet()) {
			byte key = entry.getKey();
			long value = entry.getValue();
			buffer.writeByte(key);
			buffer.writeLong(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeIBMap(Map<Integer, Byte> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (Entry<Integer, Byte> entry : values.entrySet()) {
			int key = entry.getKey();
			byte value = entry.getValue();
			buffer.writeInt(key);
			buffer.writeByte(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeIIMap(Map<Integer, Integer> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (Entry<Integer, Integer> entry : values.entrySet()) {
			int key = entry.getKey();
			int value = entry.getValue();
			buffer.writeInt(key);
			buffer.writeInt(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeILMap(Map<Integer, Long> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (Entry<Integer, Long> entry : values.entrySet()) {
			int key = entry.getKey();
			long value = entry.getValue();
			buffer.writeInt(key);
			buffer.writeLong(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeInt(int value) {
		buffer.writeInt(value);
		return this;
	}

	@Override
	public BufferWrapper writeInts(Collection<Integer> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (int value : values) {
			buffer.writeInt(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeLIMap(Map<Long, Integer> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (Entry<Long, Integer> entry : values.entrySet()) {
			long key = entry.getKey();
			int value = entry.getValue();
			buffer.writeLong(key);
			buffer.writeInt(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeLLMap(Map<Long, Long> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (Entry<Long, Long> entry : values.entrySet()) {
			long key = entry.getKey();
			long value = entry.getValue();
			buffer.writeLong(key);
			buffer.writeLong(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeLong(long value) {
		buffer.writeLong(value);
		return this;
	}

	@Override
	public BufferWrapper writeLongs(Collection<Long> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (long value : values) {
			buffer.writeLong(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public BufferWrapper writeLongs(long[] values) {
		if (values == null || values.length <= 0) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.length;
		buffer.writeShort(size);
		int realSize = 0;
		for (long value : values) {
			buffer.writeLong(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

	@Override
	public int writerIndex() {
		int writerIndex = buffer.writerIndex();
		return writerIndex;
	}

	@Override
	public BufferWrapper writerIndex(int writerIndex) {
		buffer.writerIndex(writerIndex);
		return this;
	}

	@Override
	public BufferWrapper writeShort(int value) {
		buffer.writeShort(value);
		return this;
	}

	/**
	 * 将short写入到指定下标中
	 * 
	 * @param value
	 * @param index
	 */
	protected void writeShort(int value, int index) {
		int curIndex = buffer.writerIndex();
		buffer.writerIndex(index);
		buffer.writeShort(value);
		int newIndex = Math.max(curIndex, index + 2);
		buffer.writerIndex(newIndex);
	}

	/**
	 * utf-8
	 * 
	 * @param value
	 */
	@Override
	public BufferWrapper writeString(String value) {
		if (value == null || value.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		byte[] bytes = value.getBytes(Default_Charset);
		short length = (short) bytes.length;
		buffer.writeShort(length);
		buffer.writeBytes(bytes);
		return this;
	}

	@Override
	public BufferWrapper writeString(String value, Charset charset) {
		if (value == null || value.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		byte[] bytes = value.getBytes(charset);
		short length = (short) bytes.length;
		buffer.writeShort(length);
		buffer.writeBytes(bytes);
		return this;
	}

	@Override
	public BufferWrapper writeStrings(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			buffer.writeShort(0);
			return this;
		}
		int sizeWriterIndex = buffer.writerIndex();
		int size = values.size();
		buffer.writeShort(size);
		int realSize = 0;
		for (String value : values) {
			writeString(value);
			realSize++;
		}
		if (realSize != size) {
			writeShort(size, sizeWriterIndex);
		}
		return this;
	}

}
