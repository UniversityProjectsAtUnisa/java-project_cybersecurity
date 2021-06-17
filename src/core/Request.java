package core;

import core.tokens.AuthToken;

import java.io.Serializable;

public class Request<T extends Serializable> implements Serializable {
    private final String endpointName;
    private final T payload;
    private final AuthToken token;

    private Request(String endpointName, T payload, AuthToken token) {
        this.endpointName = endpointName;
        this.payload = payload;
        this.token = token;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public T getPayload() {
        return payload;
    }
    
    public AuthToken getToken() {
        return token;
    }

    public static <T extends Serializable> Request<T> make(String endpointName, T payload, AuthToken token) {
        return new Request<>(endpointName, payload, token);
    }

    @Override
    public String toString() {
        return "Request: {endpointName: " + getEndpointName() + " payload: " + getPayload() + "}";
    }
}
