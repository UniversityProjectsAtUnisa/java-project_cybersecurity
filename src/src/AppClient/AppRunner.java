package src.AppClient;

import utils.AppTimer;
import utils.Config;

import java.util.logging.Logger;


public class AppRunner {
    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", Config.LOGGER_FMT);
        AppTimer globalAppTimer = AppTimer.getInstance();
        AppClient[] clients = new AppClient[Config.CLIENT_COUNT];

        for (int i = 0; i < clients.length; i++) {
            clients[i] = new AppClient();
        }

        Logger.getGlobal().info("STARTING LOGIN PHASE");
        for (AppClient client: clients) {
            boolean success = client.register();
            if (!success) throw new RuntimeException("Register Failed!");
            success = client.login();
            if (!success) throw new RuntimeException("Login Failed!");
        }
        Logger.getGlobal().info("LOGIN PHASE COMPLETED");

        Logger.getGlobal().info("STARTING GLOBAL CLOCK");
        globalAppTimer.start();
    }
}
