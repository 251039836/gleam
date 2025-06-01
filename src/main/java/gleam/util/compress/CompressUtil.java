package gleam.util.compress;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gleam.util.compress.impl.GzipCompressor;
import gleam.util.compress.impl.UnCompressor;
import gleam.util.compress.impl.ZipCompressor;

/**
 * 压缩工具类
 * 
 * @author hdh
 *
 */
public class CompressUtil {

    private static final Logger logger = LoggerFactory.getLogger(CompressUtil.class);

    private static final Map<Byte, Compressor> CompressorMap = new HashMap<>();

    static {
        CompressorMap.put(Compressor.NO_COMPRESS, UnCompressor.getInstance());
        CompressorMap.put(Compressor.ZIP, ZipCompressor.getInstance());
        CompressorMap.put(Compressor.GZIP, GzipCompressor.getInstance());
        // TODO
    }

    /**
     * 压缩
     * 
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] compress(byte[] data) throws IOException {
        if (data == null) {
            return data;
        }
        Compressor compressor = CompressorMap.get(Compressor.DEFAULT_COMPRESS_TYPE);
        byte[] compressData = compressor.compress(data);
        return compressData;
    }

    /**
     * 使用指定压缩方式进行压缩
     * 
     * @param data
     * @param compressType
     * @return
     * @throws IOException
     */
    public static byte[] compress(byte[] data, byte compressType) throws IOException {
        if (data == null) {
            return data;
        }
        Compressor compressor = CompressorMap.get(compressType);
        if (compressor == null) {
            if (compressType >= 0) {
                logger.warn("compress fail.compressor[{}] is null", compressType);
            }
            return data;
        }
        return compressor.compress(data);
    }

    /**
     * 解压
     * 
     * @param data
     * @return
     * @throws IOException
     */
    public static byte[] uncompress(byte[] data) throws IOException {
        if (data == null) {
            return data;
        }
        Compressor compressor = CompressorMap.get(Compressor.DEFAULT_COMPRESS_TYPE);
        byte[] uncompreeData = compressor.uncompress(data);
        return uncompreeData;
    }

    /**
     * 使用指定压缩方式进行解压
     * 
     * @param data
     * @param compressType
     * @return
     * @throws IOException
     */
    public static byte[] uncompress(byte[] data, byte compressType) throws IOException {
        if (data == null) {
            return data;
        }
        Compressor compressor = CompressorMap.get(compressType);
        if (compressor == null) {
            if (compressType >= 0) {
                logger.warn("uncompress fail.compressor[{}] is null", compressType);
            }
            return data;
        }
        return compressor.uncompress(data);
    }

}
