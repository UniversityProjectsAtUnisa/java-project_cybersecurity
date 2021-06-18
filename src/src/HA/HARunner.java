package src.HA;

import java.io.IOException;

public class HARunner {
    public static void main(String[] args) throws IOException {
        new HAServer("changeit").start();
    }
}
