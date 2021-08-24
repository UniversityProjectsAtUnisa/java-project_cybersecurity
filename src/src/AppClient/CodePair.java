package src.AppClient;

import java.io.Serializable;

public class CodePair implements Serializable {
    private final byte[] code;
    private final long instant;

    public CodePair(byte[] code, long instant) {
        this.code = code;
        this.instant = instant;
    }

    public byte[] getCode() {
        return code;
    }

    public long getInstant() {
        return instant;
    }
}
