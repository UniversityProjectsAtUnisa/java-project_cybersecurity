
import core.SSLServer;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import src.AppClient.AppClient;
import src.AppServer.AppServer;
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
public class MainRunner {

    public static void main(String[] args) throws Exception {
        System.setProperty("javax.net.ssl.keyStore", "src/core/keys/official_certificates/HAKeystore.jks");
        System.setProperty("javax.net.ssl.trustStore", "src/core/keys/official_certificates/clientTruststore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
        
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        GlobalTimer timer = new GlobalTimer(5000L);
        SSLServer sServer = new SSLServer(4000, false);
        new Thread(sServer).start();
        AppClient appClient = new AppClient(4000, timer, false);
        new Thread(appClient).start();
    }
}
