package src.HA;

import utils.Config;

import java.io.IOException;

public class HARunner {
    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", Config.LOGGER_FMT);
        new HAServer("changeit").start();
    }
}
