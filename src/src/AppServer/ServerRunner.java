package src.AppServer;

import utils.Config;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ServerRunner {
    public static void main(String[] args) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        System.setProperty("java.util.logging.SimpleFormatter.format", Config.LOGGER_FMT);
        new AppServer("changeit").start();
    }
}
