package utils;

import java.io.Serializable;

public class UseNotificationMessage implements Serializable {
    
    private final byte[] swabCode;
    private final String cf;

    public UseNotificationMessage(byte[] swabCode, String cf) {
        this.swabCode = swabCode;
        this.cf = cf;
    }

    public byte[] getSwabCode() {
        return swabCode;
    }

    public String getCf() {
        return cf;
    }
}
