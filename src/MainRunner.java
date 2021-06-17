import src.AppClient.AppClient;
import src.AppServer.AppServer;
import utils.AppTimer;
import utils.Config;


public class MainRunner {
    public static void main(String[] args) {
        /*
        AppTimer globalAppTimer = AppTimer.getInstance();
        AppServer appServer = new AppServer();
        AppClient[] clients = new AppClient[Config.CLIENT_COUNT];

        for (int i = 0; i < clients.length; i++) {
            clients[i] = new AppClient();
        }

        for (AppClient client: clients) {
            client.login();
        }

        globalAppTimer.start();
         */

        AppServer server = new AppServer();

        System.out.println(server.getSalt1());
    }
}
