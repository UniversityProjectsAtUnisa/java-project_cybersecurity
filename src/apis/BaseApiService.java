package apis;

import core.Response;
import core.SSLClient;
import core.tokens.AuthToken;
import exceptions.RequestFailedException;

import java.io.Serializable;

public abstract class BaseApiService {
    private final SSLClient client;

    public BaseApiService(String keyStore, String trustStore, String password, String hostIp, int hostPort) {
        client = new SSLClient(keyStore, trustStore, password, hostIp, hostPort);
    }

    protected Response sendRequest(String endpoint, Serializable data, AuthToken token, String errMsg) {
        Response res = client.sendRequest(endpoint, data, token);
        if (!res.isSuccess()) {
            throw new RequestFailedException("REQUEST FAILED " + errMsg + ": " + res.getPayload());
        }
        return res;
    }

    protected Response sendRequest(String endpoint, Serializable data, String errMsg) {
        return sendRequest(endpoint, data, null, errMsg);
    }
}
