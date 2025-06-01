package gleam.util.compress.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import gleam.util.compress.Compressor;

/**
 * gzip格式进行压缩/解压缩
 * 
 * @author hdh
 *
 */
public class GzipCompressor implements Compressor {
    private static GzipCompressor instance = new GzipCompressor();

    public static GzipCompressor getInstance() {
        return instance;
    }

    @Override
    public byte[] compress(byte[] data) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(byteStream)) {
            gzip.write(data);
            gzip.finish();
            byte[] zipBytes = byteStream.toByteArray();
            return zipBytes;
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public byte[] uncompress(byte[] data) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
                GZIPInputStream gzip = new GZIPInputStream(in);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            int num = -1;
            while ((num = gzip.read(buf, 0, buf.length)) != -1) {
                out.write(buf, 0, num);
            }
            byte[] bytes = out.toByteArray();
            out.flush();
            return bytes;
        } catch (IOException e) {
            throw e;
        }
    }

}
