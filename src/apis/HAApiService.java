package apis;

import core.Response;
import java.util.LinkedList;
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
    
    public boolean sendSwabCodes(LinkedList<String> codes){
        Response res = sendRequest("ADD_SWAB_CODES", codes, null, "sendSwabCode");
        return (boolean) res.getPayload();
    }
    
    public boolean sendUserIds(LinkedList<Integer> userIds){
        Response res = sendRequest("SEND_USER_IDS", userIds, null, "sendUserIds");
        return (boolean) res.getPayload();
    }
}
