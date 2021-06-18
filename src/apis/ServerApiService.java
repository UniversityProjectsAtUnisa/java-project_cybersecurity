package apis;

import core.Response;
import core.SSLClient;
import core.tokens.AuthToken;
import exceptions.RequestFailedException;
import utils.ContactReportMessage;
import utils.Credentials;
import entities.Notification;
import utils.Config;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

public class ServerApiService {

    private static final String PASSWORD = "changeit";
    private final SSLClient client;

    public ServerApiService() {
        client = new SSLClient(null, Config.CLIENT_TRUSTSTORE, PASSWORD, Config.APP_SERVER_IP, Config.APP_SERVER_PORT);
    }

    private Response sendRequest(String endpoint, Serializable data, AuthToken token, String errMsg) {
        Response res = client.sendRequest(endpoint, data, token);
        if (!res.isSuccess()) {
            throw new RequestFailedException("REQUEST FAILED: " + errMsg);
        }
        return res;
    }

    public boolean register(Credentials credentials) {
        Response res = sendRequest("register", credentials, null, "register");
        return (boolean) res.getPayload();
    }

    public AuthToken login(Credentials credentials) {
        Response res = sendRequest("login", credentials, null, "login");
        return (AuthToken) res.getPayload();
    }

    public void reportContact(int id, int duration, Timestamp startDate, AuthToken token) {
        sendRequest("createReport", new ContactReportMessage(id, duration, startDate), token, "reportContact");
    }

    public List<Notification> getNotifications(AuthToken token) {
        Response res = sendRequest("getNotifications", null, token, "getNotifications");
        return (List<Notification>) res.getPayload();
    }
}
