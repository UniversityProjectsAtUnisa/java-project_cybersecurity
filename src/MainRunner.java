import src.AppClient.AppClient;
import utils.AppTimer;
import utils.Config;


public class MainRunner {
    public static void main(String[] args) {
        AppTimer globalAppTimer = AppTimer.getInstance();
        AppClient[] clients = new AppClient[Config.CLIENT_COUNT];

        for (int i = 0; i < clients.length; i++) {
            clients[i] = new AppClient();
        }
        
        for (AppClient client: clients) {
            client.login();
        }

        globalAppTimer.start();
    }
}
