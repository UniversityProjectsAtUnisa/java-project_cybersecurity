package core;

import java.io.Serializable;

public class Response<T extends Serializable> implements Serializable {
    private final T payload;

    private Response(T payload) {
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }

    public static <T extends Serializable> Response<T> make(T payload) {
        return new Response<>(payload);
    }

    @Override
    public String toString() {
        return "Response: {payload: " + this.getPayload().toString() + "}";
    }
}
