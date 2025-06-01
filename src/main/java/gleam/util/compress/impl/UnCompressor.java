package gleam.util.compress.impl;

import gleam.util.compress.Compressor;

/**
 * 不压缩
 * 
 * @author hdh
 *
 */
public class UnCompressor implements Compressor {
    private static UnCompressor instance = new UnCompressor();

    public static UnCompressor getInstance() {
        return instance;
    }

    @Override
    public byte[] compress(byte[] data) {
        return data;
    }

    @Override
    public byte[] uncompress(byte[] data) {
        return data;
    }

}
