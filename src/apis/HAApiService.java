package apis;

import core.Response;
import utils.Config;

/**
 * Used by AppServer
 */
public class HAApiService extends BaseApiService {
    public HAApiService() {
        super(null, null, null, Config.HA_SERVER_IP, Config.HA_SERVER_PORT);
    }

    public boolean checkCf(String cf) {
        Response res = sendRequest("CF_IS_VALID", cf, null, "checkCf");
        return (boolean) res.getPayload();
    }
}
