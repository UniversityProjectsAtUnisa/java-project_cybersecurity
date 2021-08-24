package utils;

import src.AppClient.AppClient;
import src.AppClient.BluetoothModule;
import src.AppServer.ServerUtils;

import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

public class AppTimer {
    private static AppTimer instance = null;
    private final List<AppClient> clients = new LinkedList<>();
    private final Timer timer = new Timer("Global Clock");

    private AppTimer() { }

    public static AppTimer getInstance() {
        if (instance == null)
            instance = new AppTimer();
        return instance;
    }

    public void start() {
        TimerTask task = new TimerTask() {
            public void run() {
                onTimeout();
            }
        };
        timer.schedule(task, 0, Config.TC);
    }

    public void subscribe(AppClient newClient) {
        clients.add(newClient);
    }

    private void onTimeout() {
        Logger.getGlobal().info("GlobalTimer Timeout");
        long instant = ServerUtils.getNow().getTime() / Config.TC;

        long n = instant % (Config.TSEME / Config.TC);
        if (n == 0) {
            clients.forEach(appClient -> appClient.startNewInterval(instant));
        } else if (n == Config.TSEME / Config.TC -2) {
            clients.forEach(AppClient::intervalRoutine);
        }

        clients.forEach(appClient -> {
            try {
                appClient.emit(instant);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        });
        BluetoothModule.populateRandomUserMap();  // FOR SIMULATION
        clients.forEach(appClient -> appClient.scan(instant));
        BluetoothModule.clearSimulationData();    // FOR SIMULATION
    }
}
