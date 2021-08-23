package utils;

import java.nio.ByteBuffer;

public class BytesUtils {
    private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    public static byte[] fromLong(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}
