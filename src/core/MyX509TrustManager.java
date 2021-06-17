/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.io.FileInputStream;
import java.io.InputStream;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author marco
 */
class MyX509TrustManager implements X509TrustManager {

    /*
      * The default X509TrustManager returned by SunX509.  We'll delegate
      * decisions to it, and fall back to the logic in this class if the
      * default X509TrustManager doesn't trust it.
     */
    X509TrustManager x509TrustManager;

    MyX509TrustManager(String keyStoreFile, String password) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        InputStream stream = MyX509TrustManager.class.getResourceAsStream(keyStoreFile);
        ks.load(stream,
                password.toCharArray());

        TrustManagerFactory tmf
                //                = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
                = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
        tmf.init(ks);

        TrustManager tms[] = tmf.getTrustManagers();
        List<TrustManager> temp = new LinkedList<>();
        for (int i = 0; i < tms.length; i++) {
            if (tms[i] instanceof X509TrustManager) {
                temp.add((X509TrustManager) tms[i]);
//                x509TrustManager = (X509TrustManager) tms[i];
//                return;
            }
        }
        System.out.println("found " + temp.size() + " trustManagers");
        System.out.println("There were " + tms.length + " trustManagers");
        x509TrustManager = (X509TrustManager) temp.get(0);
        if (temp.size() > 0) {
            return;
        }
        throw new Exception("Couldn't initialize");
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        System.out.println("Server is checking if client is trusted " + authType);

        for (X509Certificate x509Certificate : chain) {
            System.out.println(x509Certificate);
        }
        x509TrustManager.checkClientTrusted(chain, authType);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        System.out.println("Client is checking if server is trusted " + authType);
        x509TrustManager.checkServerTrusted(chain, authType);
        System.out.println("Server is valid");
    }

    public X509Certificate[] getAcceptedIssuers() {
        return x509TrustManager.getAcceptedIssuers();
    }
}
