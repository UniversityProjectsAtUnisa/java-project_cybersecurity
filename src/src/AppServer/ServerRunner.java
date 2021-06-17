package src.AppServer;

import java.io.IOException;

public class ServerRunner {
    public static void main(String[] args) throws IOException {
        new AppServer().start();
    }
}
