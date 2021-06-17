package src.AppServer;

import java.io.IOException;

public class ServerRunner {
    public static void main(String[] args) throws IOException {
        System.setProperty("javax.net.debug", "ssl");
        new AppServer().start();
    }
}
