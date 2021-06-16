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
 */

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