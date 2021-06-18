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

    public void reportContact(int id, int duration, Timestamp startDate, AuthToken token) {
        sendRequest("createReport", new ContactReportMessage(id, duration, startDate), token, "reportContact");
    }

    public List<Notification> getNotifications(AuthToken token) {
        Response res = sendRequest("getNotifications", null, token, "getNotifications");
        return (List<Notification>) res.getPayload();
    }
}
