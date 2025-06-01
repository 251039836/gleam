package gleam.util.compress.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import gleam.util.compress.Compressor;

/**
 * zip格式进行压缩/解压缩
 * 
 * @author hdh
 *
 */
public class ZipCompressor implements Compressor {
    private static ZipCompressor instance = new ZipCompressor();

    public static ZipCompressor getInstance() {
        return instance;
    }

    @Override
    public byte[] compress(byte[] data) throws IOException {

        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(); ZipOutputStream zipStream = new ZipOutputStream(byteStream)) {
            ZipEntry zipEntry = new ZipEntry("zip");
            zipEntry.setSize(data.length);
            zipStream.putNextEntry(zipEntry);
            zipStream.write(data);
            zipStream.closeEntry();
            byte[] zipBytes = byteStream.toByteArray();
            return zipBytes;
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public byte[] uncompress(byte[] data) throws IOException {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
                ZipInputStream zipStream = new ZipInputStream(byteStream);
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while (zipStream.getNextEntry() != null) {
                byte[] buf = new byte[1024];
                int num = -1;
                while ((num = zipStream.read(buf, 0, buf.length)) != -1) {
                    out.write(buf, 0, num);
                }
            }
            byte[] bytes = out.toByteArray();
            out.flush();
            return bytes;
        } catch (IOException e) {
            throw e;
        }
    }

}
