package src.AppClient;

import utils.AppTimer;
import utils.Config;

public class AppRunner {
    public static void main(String[] args) {
        AppTimer globalAppTimer = AppTimer.getInstance();
        AppClient[] clients = new AppClient[Config.CLIENT_COUNT];

        for (int i = 0; i < clients.length; i++) {
            clients[i] = new AppClient();
            clients[i].login();
        }

        globalAppTimer.start();
    }
}
