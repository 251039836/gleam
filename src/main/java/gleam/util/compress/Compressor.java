package gleam.util.compress;

import java.io.IOException;

public interface Compressor {

    /**
     * 不压缩
     */
    byte NO_COMPRESS = -1;

    /**
     * 自动判断是否压缩
     */
    byte AUTO = 0;

    /**
     * zip格式压缩
     */
    byte ZIP = 1;
    /**
     * gzip格式压缩
     */
    byte GZIP = 2;
    /**
     * 默认压缩格式
     */
    byte DEFAULT_COMPRESS_TYPE = ZIP;

    byte[] compress(byte[] data) throws IOException;

    byte[] uncompress(byte[] data) throws IOException;
}
