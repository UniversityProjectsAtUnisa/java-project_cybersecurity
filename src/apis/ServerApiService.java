package apis;

import core.Response;
import core.SSLClient;
import core.tokens.AuthToken;
import utils.ContactReportMessage;
import utils.Credentials;
import entities.Notification;
import utils.Config;
import java.sql.Timestamp;

import java.time.LocalDateTime;

public class ServerApiService {
    private static final String PASSWORD = "changeit";
    private final SSLClient client;

    public ServerApiService() {
        client = new SSLClient(null, Config.CLIENT_TRUSTSTORE, PASSWORD, Config.APP_SERVER_IP, Config.APP_SERVER_PORT);
    }

    public boolean register(Credentials credentials) {
        Response res = client.sendRequest("REGISTER", credentials, null);
        return (boolean) res.getPayload();
    }

    public AuthToken login(Credentials credentials) {
        Response res = client.sendRequest("LOGIN", credentials, null);
        return (AuthToken) res.getPayload();
    }

    public void reportContact(int id, int duration, Timestamp startDate, AuthToken token) {
        client.sendRequest("REPORT_CONTACT", new ContactReportMessage(id, duration, startDate), token);
    }

    public Notification[] getNotifications(AuthToken token) {
        Response res = client.sendRequest("GET_NOTIFICATIONS", null, token);
        return (Notification[]) res.getPayload();
    }
}
