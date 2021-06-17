/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import utils.Config;

public abstract class SSLServer {

    private final int BACKLOG = 100;
    private final SSLServerSocket serverSocket;

    public SSLServer(String keyStore, String trustStore, String password, int port, boolean useClientAuth) throws IOException {
        System.setProperty("javax.net.ssl.keyStore", Config.KEYSTORES_BASE_PATH + keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", password);
        System.setProperty("javax.net.ssl.trustStore", Config.KEYSTORES_BASE_PATH + trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", password);

        SSLServerSocketFactory sslFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        serverSocket = (SSLServerSocket) sslFactory.createServerSocket(port, BACKLOG);
        serverSocket.setEnabledProtocols(new String[]{"TLSv1.3"});
        serverSocket.setNeedClientAuth(useClientAuth);
        Logger.getGlobal().log(Level.INFO, "SERVER CREATED WITH CLIENTAUTH={0}", useClientAuth);
    }

    public void start() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        Logger.getGlobal().info("SERVER STARTED");
        while (true) {
            try {
                try (SSLSocket acceptedSocket = (SSLSocket) serverSocket.accept()) {
                    Logger.getGlobal().info("ACCEPTED INCOMING CONNECTION");
                    in = new ObjectInputStream(acceptedSocket.getInputStream());
                    out = new ObjectOutputStream(acceptedSocket.getOutputStream());

                    Request req = (Request) in.readObject();
                    Logger.getGlobal().info(req.toString());
                    Response res = handleRequest(req);

                    out.writeObject(res);
                    out.flush();
                }
            } catch (ClassNotFoundException | IOException e) {
                Logger.getGlobal().log(Level.WARNING, e.getMessage());
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    abstract protected Response handleRequest(Request req);
}
