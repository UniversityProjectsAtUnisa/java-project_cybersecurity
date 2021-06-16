/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

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

/**
 *
 * @author marco
 */
public class SSLClient {

    private int port;
    private boolean withClientAuthentication = false;

    public SSLClient(int port, boolean withClientAuthentication) throws Exception {
//        SSLContext sslContext = createSSLContext();
//        SSLSocketFactory factory = sslContext.getSocketFactory();
        this.port = port;
        this.withClientAuthentication = withClientAuthentication;

    }

    private SSLSocket createNewSocket() throws IOException {
        System.setProperty("javax.net.ssl.trustStore", "src/core/keys/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket newSocket = (SSLSocket) factory.createSocket("localhost", port);
        newSocket.startHandshake();
        return newSocket;
    }

    public SSLClient(int port) throws Exception {
        this(port, false);
    }

    static SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        InputStream stream = SSLClient.class.getResourceAsStream("src/core/keys/truststore.jks");
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        char[] trustStorePassword = "changeit".toCharArray();
        trustStore.load(stream, trustStorePassword);
        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(trustStore);
        TrustManager[] trustManager = factory.getTrustManagers();
        sslContext.init(null, trustManager, null);
        return sslContext;
    }

    public Response sendRequest(String endpointName) throws IOException, ClassNotFoundException {
        SSLSocket socket = this.createNewSocket();

        Request request = new Request(endpointName);

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