package utils;

import java.io.Serializable;

public class UseNotificationMessage implements Serializable {
    
    private final String swabCode;
    private final String cf;

    public UseNotificationMessage(String swabCode, String cf) {
        this.swabCode = swabCode;
        this.cf = cf;
    }

    public String getSwabCode() {
        return swabCode;
    }

    public String getCf() {
        return cf;
    }
}
