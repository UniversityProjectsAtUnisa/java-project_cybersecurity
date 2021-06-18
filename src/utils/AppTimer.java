package utils;

import src.AppClient.AppClient;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
        for (AppClient appClient: clients) {
            appClient.scanAndEmit();
        }
    }
}
