/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.Timer;
import java.util.TimerTask;
import java.time.LocalDateTime;

/**
 *
 * @author marco
 */
public class GlobalTimer extends Timer {

    private LocalDateTime lastNotification;

    public GlobalTimer(long period) {
        super("GlobalTimer");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                taskFunction();
            }
        };
        this.schedule(task, period, period);
    }

    public boolean timeToBroadcast() {
        LocalDateTime now = LocalDateTime.now();
        return lastNotification.plusSeconds(5).isAfter(now);
    }

    private synchronized void taskFunction() {
        lastNotification = LocalDateTime.now();
        System.out.println("Timer notify all");
        notifyAll();
    }
}
