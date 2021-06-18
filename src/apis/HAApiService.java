package apis;

import core.Response;
import core.SSLClient;
import utils.Config;

/**
 * Used by AppServer
 */
public class HAApiService {
    private final SSLClient client;

    public HAApiService(String password) {
        client = new SSLClient(null, null, null, Config.HA_SERVER_IP, Config.HA_SERVER_PORT);
    }

    public boolean checkCf(String cf) {
        Response res = client.sendRequest("CF_IS_VALID", cf, null);
        return (boolean) res.getPayload();
    }
}
