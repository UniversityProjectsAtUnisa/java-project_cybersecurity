package core;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.cert.CertPathValidator;
import java.util.Arrays;
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

    public SSLServer(String keyStore, String trustStore, String password, int port) throws IOException {
        this(keyStore, trustStore, password, port, false);
    }

    public void start() {
        Logger.getGlobal().info("SERVER STARTED");
        while (true) {
            try (SSLSocket acceptedSocket = (SSLSocket) serverSocket.accept()) {
                Logger.getGlobal().info("ACCEPTED INCOMING CONNECTION");

                try (ObjectInputStream in = new ObjectInputStream(acceptedSocket.getInputStream())) {
                    Request req = (Request) in.readObject();
                    Logger.getGlobal().info(req.toString());

                    Response res = handleRequest(req);
                    try (ObjectOutputStream out = new ObjectOutputStream(acceptedSocket.getOutputStream())) {
                        out.writeObject(res);
                        out.flush();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                Logger.getGlobal().log(Level.WARNING, e.getMessage());
            }
        }
    }

    abstract protected Response handleRequest(Request req);
}
