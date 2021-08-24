package utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class BytesUtils {

    public static int TIMESTAMP_BYTES_SIZE = Timestamp.valueOf(LocalDateTime.MIN).toString().getBytes().length;

    public static byte[] zeroes(int length) {
        return new byte[length];
    }

    public static byte[] fromLong(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long toLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(0, bytes);
        return buffer.getLong();
    }

    public static byte[] fromTimestamp(Timestamp t) {
        if (t == null) {
            return BytesUtils.zeroes(BytesUtils.TIMESTAMP_BYTES_SIZE);
        }
        return t.toString().getBytes();
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static byte[] concat(byte[]... bytesArray) {
        int i = 0;
        byte[] concatRes = null;
        for (byte[] bs : bytesArray) {
            if (i == 0) {
                concatRes = bs;
            } else {
                concatRes = BytesUtils.concat(concatRes, bs);
            }
            i++;
        }
        return concatRes;
    }

    public static String toString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
