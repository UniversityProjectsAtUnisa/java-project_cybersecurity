package apis;

import core.Response;
import core.SSLClient;
import core.tokens.AuthToken;
import exceptions.RequestFailedException;
import src.AppClient.CodePair;
import src.AppClient.Seed;
import utils.ContactReportMessage;
import utils.Credentials;
import entities.Notification;
import utils.Config;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ServerApiService extends BaseApiService {

    private static final String PASSWORD = "changeit";

    public ServerApiService() {
        super(null, Config.CLIENT_TRUSTSTORE, PASSWORD, Config.APP_SERVER_IP, Config.APP_SERVER_PORT);
    }

    public boolean register(Credentials credentials) {
        Response res = sendRequest("register", credentials, null, "register");
        return (boolean) res.getPayload();
    }

    public AuthToken login(Credentials credentials) {
        Response res = sendRequest("login", credentials, null, "login");
        return (AuthToken) res.getPayload();
    }

    public boolean isUserPositive(AuthToken token) {
        Response res = sendRequest("isPositive", null, token, "isUserPositive");
        return (boolean) res.getPayload();
    }

    public boolean sendSeedsAndReceivedCodes(HashMap<Seed, List<CodePair>> history, AuthToken token) {
        Response res = sendRequest("sendPositiveData", history, token, "sendSeedsAndReceivedCodes");
        return (boolean) res.getPayload();
    }

    public List<Seed> getPositiveSeeds(AuthToken token) {
        Response res = sendRequest("getPositiveSeeds", null, token, "getPositiveSeeds");
        return (LinkedList<Seed>) res.getPayload();
    }

    public boolean reportContacts(LinkedList<Seed> seeds, AuthToken token) {
        Response res = sendRequest("isAtRisk", seeds, token, "reportContacts");
        return (boolean) res.getPayload();
    }
}
