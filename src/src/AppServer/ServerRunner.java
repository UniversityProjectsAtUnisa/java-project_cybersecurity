package src.AppServer;

import utils.Config;

import java.io.IOException;

public class ServerRunner {
    public static void main(String[] args) throws IOException {
        System.setProperty("java.util.logging.SimpleFormatter.format", Config.LOGGER_FMT);
        new AppServer("changeit").start();
    }
}
