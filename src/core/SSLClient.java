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
import java.io.*;
import java.util.logging.Logger;

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
            Logger.getGlobal().info("STARTING HANDSHAKE");
            socket.startHandshake();
            Logger.getGlobal().info("HANDSHAKE COMPLETED");

            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                Logger.getGlobal().info("CREATING REQUEST");
                Request req = Request.make(endpoint, data, token);
                Logger.getGlobal().info("REQ CREATED: " + req);
                out.writeObject(req);
                Logger.getGlobal().info("REQ SENT");
                out.flush();
                Logger.getGlobal().info("REQ COMPLETED: " + req);
                Response res = (Response) in.readObject();
                socket.close();
                return res;
            } catch(Exception e) {
                Logger.getGlobal().warning("RequestFailed: " + e.getMessage());
                throw e;
            }
        } catch (IOException | ClassNotFoundException e) {
            Logger.getGlobal().warning("RequestFailed: " + e.getMessage());
            throw new RequestFailedException(e.getMessage());
        }
    }
}
