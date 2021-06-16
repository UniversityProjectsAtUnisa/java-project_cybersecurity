/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppClient;

import core.Response;
import core.SSLClient;
import java.io.IOException;
import utils.GlobalTimer;

/**
 *
 * @author marco
 */
public class AppClient implements Runnable {

    private int port;
    private final GlobalTimer timer;
    private SSLClient client;

    public AppClient(int port, GlobalTimer timer, boolean withClientAuthentication) throws Exception {
        this.port = port;
        this.timer = timer;
        this.client = new SSLClient(this.port, withClientAuthentication);
        System.out.println("AppClient initialized");

    }

    public AppClient(int port, GlobalTimer timer) throws Exception {
        this(port, timer, false);
    }

    public void startBluetoothPhase() {
        System.out.println("Bluetooth started");
        try {
            Response res = this.client.sendRequest("login");
            System.out.println("Il client ha ricevuto come risposta: " + res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (true) {
            synchronized (this.timer) {
                try {
                    System.out.println("Waiting on timer");
                    this.timer.wait();
                    System.out.println("Awaken");
                    if (this.timer.timeToBroadcast()) {
                        startBluetoothPhase();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
