package src.AppClient;

public class Credentials {
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
}
