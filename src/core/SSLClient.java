/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import core.tokens.AuthToken;
import exceptions.RequestFailedException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import utils.Config;

public class SSLClient {

    private final SSLSocketFactory sslFactory;
    private final String hostIp;
    private final int port;

    public SSLClient(String keyStore, String trustStore, String password, String hostIp, int port) {

        if (keyStore != null) {
            System.setProperty("javax.net.ssl.keyStore", Config.KEYSTORES_BASE_PATH + keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", password);
        }
        System.setProperty("javax.net.ssl.trustStore", Config.KEYSTORES_BASE_PATH + trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", password);

        sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        this.hostIp = hostIp;
        this.port = port;
    }

    public Response sendRequest(String endpoint, Serializable data, AuthToken token) throws RequestFailedException {
        try {
            SSLSocket socket = (SSLSocket) sslFactory.createSocket(hostIp, port);
            socket.startHandshake();

            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                Request req = Request.make(endpoint, data, token);
                out.writeObject(req);
                out.flush();
                return (Response) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RequestFailedException(e.getMessage());
        }
    }
}
