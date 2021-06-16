
import core.SSLServer;
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
//        System.setProperty("javax.net.debug", "ssl");

        GlobalTimer timer = new GlobalTimer(5000L);
        SSLServer sServer = new SSLServer(4000);
        new Thread(sServer).start();
        AppClient appClient = new AppClient(4000, timer);
        new Thread(appClient).start();
        AppClient appClient1 = new AppClient(4000, timer);
        new Thread(appClient1).start();
        AppClient appClient2 = new AppClient(4000, timer);
        new Thread(appClient2).start();
    }
}
