/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import static core.SSLClient.createSSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.util.concurrent.TimeUnit;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.security.auth.x500.X500Principal;
import utils.Utils;

public class SSLServer implements Runnable {

    private SSLServerSocket sSock;
    private String identityString;

    public SSLServer(int port, boolean withClientAutentication, String identityString) throws Exception {
        SSLContext sslContext = createSSLContext();
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        SSLServerSocket newSock = (SSLServerSocket) factory.createServerSocket(port);
        newSock.setNeedClientAuth(withClientAutentication);
        this.sSock = newSock;
        this.identityString = identityString;
        System.out.println("SSLServer initialized");
    }

    public SSLServer(int port) throws Exception {
        this(port, false, "");
    }

    static SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        X509KeyManager[] keyManager = new X509KeyManager[]{new MyKeyManager("src/core/keys/keystore.jks", "changeit".toCharArray(), "ssltest")};
        InputStream stream = SSLClient.class.getResourceAsStream("src/core/keys/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] trustStorePassword = "changeit".toCharArray();
        trustStore.load(stream, trustStorePassword);
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(trustStore);
        TrustManager[] trustManager = factory.getTrustManagers();
        sslContext.init(keyManager, trustManager, null);
        return sslContext;
    }

    public SSLSocket accept() throws IOException {
        boolean needsClientAuth = this.sSock.getNeedClientAuth();
        SSLSocket acceptedSocket = (SSLSocket) sSock.accept();
        acceptedSocket.startHandshake();
        if (needsClientAuth) {
            verifyIdentity(acceptedSocket.getSession());
        }
        return acceptedSocket;
    }

    private boolean verifyIdentity(SSLSession session) throws SSLPeerUnverifiedException {
        X500Principal id = (X500Principal) session.getPeerPrincipal();
        System.out.println("principal: " + id.getName());
        return id.getName().equals(identityString); // like "CN=localhost,OU=Students,O=unisa,C=IT"
    }

    @Override
    public void run() {
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        while (true) {
            try {
                SSLSocket acceptedSocket = (SSLSocket) this.accept();
                System.out.println("Accepted incoming connection");
                in = new ObjectInputStream(acceptedSocket.getInputStream());
                out = new ObjectOutputStream(acceptedSocket.getOutputStream());

                Request req = (Request) in.readObject();

                System.out.println("Il server ha rilevato " + req.getEndpointName());

                Response res = new Response();
                out.writeObject(res);
                out.flush();

            } catch (ClassNotFoundException | IOException e) {
            } finally {
                try {
                    in.close();
                } catch (Exception e) {
                }
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

}
