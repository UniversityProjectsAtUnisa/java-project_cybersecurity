package utils;

import java.io.Serializable;

public class Credentials implements Serializable {

    private final String cf;
    private final String password;

    public Credentials(String cf, String password) {
        this.cf = cf;
        this.password = password;
    }

    public String getCf() {
        return cf;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "[ Credentials: {cf: " + getCf() + ", password: " + password + "} ]";
    }
}
