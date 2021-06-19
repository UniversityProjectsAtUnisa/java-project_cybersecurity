package apis;

import core.Response;
import utils.Config;
import utils.UseNotificationMessage;

import java.io.Serializable;

public class RestrictedServerApiService extends BaseApiService {

    public RestrictedServerApiService() {
        super(null, null, null, Config.APP_SERVER_IP, Config.RESTRICTED_APP_SERVER_PORT);
    }

    public boolean notifyPositiveUser(String cf) {
        Response res = sendRequest("NOTIFY_POSITIVE_USER", cf, "notifyPositiveUser");
        return (boolean) res.getPayload();
    }

    public boolean useNotification(String tamponCode, String cf) {
        Serializable payload = new UseNotificationMessage(tamponCode, cf);
        Response res = sendRequest("USE_NOTIFICATION", payload, "useNotification");
        return (boolean) res.getPayload();
    }
}
