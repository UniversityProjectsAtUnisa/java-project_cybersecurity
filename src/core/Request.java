/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.Serializable;

/**
 *
 * @author marco
 * @param <T>
 */
public class Request<T extends Serializable> implements Serializable {

    private final String endpointName;
    private final T payload;

    private Request(String endpointName, T payload) {
        this.endpointName = endpointName;
        this.payload = payload;
    }

    public String getEndpointName() {
        return endpointName;
    }

    public T getPayload() {
        return payload;
    }

    public static <T extends Serializable> Request<T> make(String endpointName, T payload) {
        return new Request<>(endpointName, payload);
    }

    @Override
    public String toString() {
        return "Request: {endpointName: " + this.getEndpointName().toString() + " " + "payload: " + this.getPayload().toString() + "}"; //To change body of generated methods, choose Tools | Templates.
    }
}
