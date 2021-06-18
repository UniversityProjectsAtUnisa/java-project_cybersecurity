package apis;

import core.Response;
import utils.Config;

public class RestrictedServerApiService extends BaseApiService {

    public RestrictedServerApiService() {
        super(null, null, null, Config.APP_SERVER_IP, Config.RESTRICTED_APP_SERVER_PORT);
    }

    public boolean notifyPositiveUser(String cf) {
        Response res = sendRequest("NOTIFY_POSITIVE_USER", cf, null, "notifyPositiveUser");
        return (boolean) res.getPayload();
    }
}
