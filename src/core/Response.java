package core;

import java.io.Serializable;

public class Response<T extends Serializable> implements Serializable {
    private final T payload;
    private final boolean success;

    private Response(T payload, boolean success) {
        this.payload = payload;
        this.success = success;
    }

    public T getPayload() {
        return payload;
    }

    public boolean isSuccess() {
        return success;
    }

    public static <T extends Serializable> Response<T> make(T payload) {
        return new Response<>(payload, true);
    }

    public static <T extends Serializable> Response<T> error(T payload) {
        return new Response<>(payload, false);
    }

    @Override
    public String toString() {
        return "Response: {payload: " + this.getPayload().toString() + "}";
    }
}
