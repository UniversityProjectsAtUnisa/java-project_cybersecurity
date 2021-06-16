/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import entities.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

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
    
    public SSLServer(int port, boolean withClientAuthentication) throws Exception {
        SSLContext sslContext = createSSLContext(withClientAuthentication);
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        SSLServerSocket newSock = (SSLServerSocket) factory.createServerSocket(port);
        newSock.setNeedClientAuth(withClientAuthentication);
        this.sSock = newSock;
        System.out.println("SSLServer initialized");
    }
    
    public SSLServer(int port) throws Exception {
        this(port, false);
    }
    
    static SSLContext createSSLContext(boolean withClientAuthentication) throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        X509KeyManager[] keyManagers = new X509KeyManager[]{new MyKeyManager("src/core/keys/official_certificates/server/serverKeystore.jks", "changeit")};
        TrustManager[] trustManagers = null;
        if (withClientAuthentication) {
            trustManagers = new TrustManager[]{
                new MyX509TrustManager("src/core/keys/official_certificates/server/serverTruststore.jks", "changeit")};
        }
        sslContext.init(keyManagers, trustManagers, SecureRandom.getInstance("DEFAULT"));
        return sslContext;
    }
    
    public SSLSocket accept() throws IOException {
        SSLSocket acceptedSocket = (SSLSocket) sSock.accept();
        acceptedSocket.startHandshake();        
        return acceptedSocket;
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
                
                System.out.println("Il server ha rilevato la richiesta: " + req);
                Response res = Response.make(new Contact());
                out.writeObject(res);
                out.flush();
                acceptedSocket.close();
                
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
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
