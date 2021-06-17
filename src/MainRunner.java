import src.AppClient.AppClient;
import src.AppServer.AppServer;
import utils.AppTimer;
import utils.Config;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;


public class MainRunner {
    public static void main(String[] args) {
        /*
        AppTimer globalAppTimer = AppTimer.getInstance();
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
        HashMap<String, String> userRegInfo = new HashMap<String, String>() {{
            put("AAAAAA00A00A000A", "password1");
            put("BBBBBB00B00B000B", "password2");
            put("CCCCCC00C00C000C", "password3");
            put("DDDDDD00D00D000D", "password4");
            put("EEEEEE00E00E000E", "password5");
        }};

        HashMap<String, String> userToken=new HashMap<>();

        userRegInfo.forEach((key, value) -> {
            try {
               boolean result =  server.register(key, value);
               if (result) {
                   userToken.put(key, server.login(key, value));
               }
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
            }
        });

        System.out.println(server.getSalt1());
    }
}
