package apis;

import core.Response;
import utils.Config;
import utils.UserNotification;

public class PublicHaApiService extends BaseApiService {
    private static final String PASSWORD = "changeit";

    public PublicHaApiService() {
        super(null, Config.CLIENT_TRUSTSTORE, PASSWORD, Config.HA_SERVER_IP, Config.PUBLIC_HA_SERVER_PORT);
    }

    public boolean useNotification(UserNotification userNotification) {
        Response res = sendRequest("USE_NOTIFICATION", userNotification, null, "useNotification");
        return (boolean) res.getPayload();
    }
}
