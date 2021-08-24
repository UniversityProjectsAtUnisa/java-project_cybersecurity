package apis;

import core.Response;
import core.tokens.AuthToken;
import entities.PositiveContact;
import src.AppClient.Seed;
import utils.Credentials;
import utils.Config;

import java.util.LinkedList;
import java.util.List;

public class ServerApiService extends BaseApiService {

    private static final String PASSWORD = "changeit";

    public ServerApiService() {
        super(null, Config.CLIENT_TRUSTSTORE, PASSWORD, Config.APP_SERVER_IP, Config.APP_SERVER_PORT);
    }

    public boolean register(Credentials credentials) {
        Response res = sendRequest("REGISTER", credentials, null, "register");
        return (boolean) res.getPayload();
    }

    public AuthToken login(Credentials credentials) {
        Response res = sendRequest("LOGIN", credentials, null, "login");
        return (AuthToken) res.getPayload();
    }

    public boolean isUserPositive(AuthToken token) {
        Response res = sendRequest("IS_POSITIVE", null, token, "isUserPositive");
        return (boolean) res.getPayload();
    }

    public boolean sendSeedsAndReceivedCodes(LinkedList<PositiveContact> history, AuthToken token) {
        Response res = sendRequest("SEND_POSITIVE_DATA", history, token, "sendSeedsAndReceivedCodes");
        return (boolean) res.getPayload();
    }

    public List<Seed> getPositiveSeeds(AuthToken token) {
        Response res = sendRequest("GET_POSITIVE_SEEDS", null, token, "getPositiveSeeds");
        return (LinkedList<Seed>) res.getPayload();
    }

    public byte[] reportContacts(LinkedList<Seed> seeds, AuthToken token) {
        Response res = sendRequest("IS_AT_RISK", seeds, token, "reportContacts");
        return (byte[]) res.getPayload();
    }
}
