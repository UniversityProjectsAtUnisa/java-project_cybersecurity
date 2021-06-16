/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import entities.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.security.*;
import java.security.Provider.Service;

/**
 *
 * @author marco
 */
public class SSLClient {

    private int port;
    private boolean withClientAuthentication = false;

    public SSLClient(int port, boolean withClientAuthentication) throws Exception {
        this.port = port;
        this.withClientAuthentication = withClientAuthentication;
    }

    private SSLSocket createNewSocket() throws Exception {
        SSLContext context = createSSLContext();
        SSLSocketFactory factory = context.getSocketFactory();
        SSLSocket newSocket = (SSLSocket) factory.createSocket("localhost", port);
        newSocket.startHandshake();
        System.out.println("DEBUG: client handshake eseguito con successo");
        return newSocket;
    }

    public SSLClient(int port) throws Exception {
        this(port, false);
    }

    public SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = new TrustManager[]{
            new MyX509TrustManager("src/core/keys/official_certificates/clientTruststore.jks", "changeit")};

        X509KeyManager[] keyManagers = null;
        if (this.withClientAuthentication) {
            keyManagers = new X509KeyManager[]{new MyKeyManager("src/core/keys/official_certificates/HAKeystore.jks", "changeit")};
        }
        sslContext.init(keyManagers, trustManagers, SecureRandom.getInstance("DEFAULT"));
        return sslContext;
    }

    public Response sendRequest(String endpointName) throws Exception {
        SSLSocket socket = this.createNewSocket();

        Request request = Request.make(endpointName, new User());

        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());) {
            out.writeObject(request);
            out.flush();
            Response response = (Response) in.readObject();
            return response;
        } catch (Exception e) {
        }
        return null;
    }
}
