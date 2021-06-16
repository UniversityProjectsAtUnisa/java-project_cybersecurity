/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppClient;

import core.SSLClient;
import java.io.IOException;
import utils.GlobalTimer;

/**
 *
 * @author marco
 */
//public class AppClient implements Runnable {
public class AppClient {

    private int port;
//    private final GlobalTimer timer;
    private SSLClient client;

//    public AppClient(int port, GlobalTimer timer) throws Exception {
    public AppClient(int port) throws Exception {
        this.port = port;
//        this.timer = timer;
        this.client = new SSLClient(this.port);
        System.out.println("AppClient initialized");
    }

    public void startBluetoothPhase() {
        System.out.println("Bluetooth started");
        try {
        client.sendRequest("login");
        } catch (ClassNotFoundException | IOException e){
            e.printStackTrace();
        }
    }

//    @Override
//    public void run() {
//
//        while (true) {
//            synchronized (this.timer) {
//                try {
//                    System.out.println("Waiting on timer");
//                    this.timer.wait();
//                    System.out.println("Awaken");
//                    if (this.timer.timeToBroadcast()) {
//                        startBluetoothPhase();
//                    }
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}
