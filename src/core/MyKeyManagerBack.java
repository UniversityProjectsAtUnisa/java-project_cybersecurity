package core;

import java.io.InputStream;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509KeyManager;
import java.io.FileInputStream;
import java.io.IOException;

public class MyKeyManagerBack {

//    private KeyStore keyStore;
//    private String alias = "mykey";
//    private char[] password;
//    
//    MyKeyManager(String keyStoreFile, String password, String alias)
//            throws IOException, GeneralSecurityException {
//        if (alias != null) {
//            this.alias = alias;
//        }
//        this.password = password.toCharArray();
//        InputStream stream = new FileInputStream(keyStoreFile);
//        keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//        keyStore.load(stream, this.password);
//    }
//
//    MyKeyManager(String keyStoreFile, String password) throws IOException, GeneralSecurityException {
//        this(keyStoreFile, password, null);
//    }
//
//    public PrivateKey getPrivateKey(String alias) {
//        try {
//            return (PrivateKey) keyStore.getKey(alias, password);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public X509Certificate[] getCertificateChain(String alias) {
//
//        try {
//            java.security.cert.Certificate[] certs = keyStore.getCertificateChain(alias);
//            if (certs == null || certs.length == 0) {
//                return null;
//            }
//            X509Certificate[] x509 = new X509Certificate[certs.length];
//            for (int i = 0; i < certs.length; i++) {
//                x509[i] = (X509Certificate) certs[i];
//            }
//            return x509;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public String chooseServerAlias(String keyType, Principal[] issuers,
//            Socket socket) {
//        return alias;
//    }
//
//    public String[] getClientAliases(String parm1, Principal[] parm2) {
//        return new String[]{alias};
//    }
//
//    public String chooseClientAlias(String keyTypes[], Principal[] issuers, Socket socket) {
//        return alias;
//    }
//
//    public String[] getServerAliases(String parm1, Principal[] parm2) {
//        return new String[]{alias};
//    }
//
//    public String chooseServerAlias(String parm1, Principal[] parm2) {
//        return alias;
//    }

}
