package utils;

import java.io.Serializable;

public record UseNotificationMessage(String swabCode, String cf) implements Serializable {

    public String getSwabCode() {
        return swabCode;
    }

    public String getCf() {
        return cf;
    }
}
