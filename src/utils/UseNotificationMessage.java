package utils;

import java.io.Serializable;

public class UseNotificationMessage implements Serializable {
    private final String tamponCode;
    private final String cf;

    public UseNotificationMessage(String tamponCode, String cf) {
        this.tamponCode = tamponCode;
        this.cf = cf;
    }

    public String getTamponCode() {
        return tamponCode;
    }

    public String getCf() {
        return cf;
    }
}
