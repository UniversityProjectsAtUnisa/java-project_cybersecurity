package core;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;

public class MyKeyManager extends X509ExtendedKeyManager {

    private String alias = "mykey";
    private final X509ExtendedKeyManager originalKeyManager;

    public MyKeyManager(String keyStoreFile, String password, String alias) throws GeneralSecurityException, IOException {
        if (alias != null) {
            this.alias = alias;
        }

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(new File(keyStoreFile)), password.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());
        this.originalKeyManager = (X509ExtendedKeyManager) kmf.getKeyManagers()[0];
    }

    public MyKeyManager(String keyStoreFile, String password) throws GeneralSecurityException, IOException {
        this(keyStoreFile, password, null);
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return alias;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return originalKeyManager.chooseServerAlias(keyType, issuers, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        return originalKeyManager.getCertificateChain(alias);
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return new String[]{alias};
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return originalKeyManager.getPrivateKey(alias);
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return originalKeyManager.getServerAliases(keyType, issuers);
    }

    @Override
    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return alias;
    }

    @Override
    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return originalKeyManager.chooseEngineServerAlias(keyType, issuers, engine);
    }
}
