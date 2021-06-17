
import core.SSLServer;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import src.AppClient.AppClient;
import utils.GlobalTimer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author marco
 */
public class ServerRunner {

    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "src/core/keys/official_certificates/server/serverKeystore.jks");
        System.setProperty("javax.net.ssl.trustStore", "src/core/keys/official_certificates/server/serverTruststore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        SSLServer sServer = new SSLServer(4000, false);

        new Thread(sServer).start();
    }

}
