package utils;

import java.io.Serializable;

public class UserNotification implements Serializable {

    private final byte[] fullSwab;
    private final String cf;

    public UserNotification(byte[] fullSwab, String cf) {
        this.fullSwab = fullSwab;
        this.cf = cf;
    }

    public byte[] getFullSwab() {
        return fullSwab;
    }

    public String getCf() {
        return cf;
    }
}
