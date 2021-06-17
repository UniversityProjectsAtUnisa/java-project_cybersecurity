package src.AppClient;

import apis.ServerApiService;
import core.tokens.AuthToken;
import utils.Credentials;
import entities.Notification;
import java.sql.Timestamp;
import utils.AppTimer;
import utils.Config;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import src.AppServer.ServerUtils;

public class AppClient {
    private static final double MAX_DISTANCE = 2.0;

    private static class ContactCounter {
        private int count = 1;
        private final Timestamp startDate;

        public ContactCounter() {
            startDate = ServerUtils.getNow();
        }

        public void increment() {
            count++;
        }
    }

    private AppClientState appState = AppClientState.NOT_LOGGED;
    private final BluetoothModule ble = new BluetoothModule();
    private final Map<Integer, ContactCounter> userCounter = new HashMap<>();
    private final ServerApiService serverApi = new ServerApiService();
    private AuthToken token;
    private final Timer notificationTimer = new Timer();

    public boolean register() {
        Credentials c = FakeInput.getNextCredential();
        return serverApi.register(c);
    }

    public boolean login() {
        Credentials c = FakeInput.getNextCredential();
        token = serverApi.login(c);
        if (token == null)
            return false;
        appState = AppClientState.LOGGED;
        AppTimer.getInstance().subscribe(this);
        startNotificationTimer();
        return true;
    }

    private void startNotificationTimer() {
        TimerTask task = new TimerTask() {
            public void run() {
                fetchNotifications();
            }
        };
        long firstDelay = (long) ((Math.random() * (36 - 12)) + 12);
        long period = 24L;
        // small values just for the simulation
        notificationTimer.schedule(task, firstDelay * 1000L, period * 1000L);
    }

    public void fetchNotifications() {
        Notification[] notifications = serverApi.getNotifications(token);
        String log = String.format("User(%d) Notifications: %s", token.getId(), Arrays.toString(notifications));
        Logger.getGlobal().info(log);
    }

    public void scanAndEmit() {
        if (appState != AppClientState.LOGGED) throw new RuntimeException();
        ble.emit(); // non blocking
        BluetoothScan[] scanResult = ble.scan(token.getId());
        Set<BluetoothScan> filteredResult = new HashSet<>();  // remove duplicates and distant scans
        Collections.addAll(
            filteredResult,
            Arrays.stream(scanResult).filter((item) -> item.getDistance() < MAX_DISTANCE).toArray(BluetoothScan[]::new)
        );
        filteredResult.forEach(this::evaluateScan);
        evaluateContactEnded(filteredResult);
    }

    private void evaluateScan(BluetoothScan scan) {
        int id = scan.getId();
        if (userCounter.containsKey(id)) {
            ContactCounter cc = userCounter.get(id);
            cc.increment();
            if (cc.count >= Config.N_CUM) {  // if reach the limit
                terminateContact(id);
            }
        } else {
            userCounter.put(id, new ContactCounter());
        }
    }

    private void evaluateContactEnded(Set<BluetoothScan> scanResult) {
        Set<Integer> currentIds = new HashSet<>(userCounter.keySet());

        Set<Integer> scannedIds = new HashSet<>();
        for (BluetoothScan scan: scanResult) {
            scannedIds.add(scan.getId());
        }

        currentIds.removeAll(scannedIds);
        currentIds.forEach(this::terminateContact);
    }

    private void terminateContact(int id) {
        ContactCounter contactCounter = userCounter.remove(id);
        System.out.printf("ReportContact(id=%d, count=%d, startDate=%s)%n", id, contactCounter.count, contactCounter.startDate);
        serverApi.reportContact(id, contactCounter.count * Config.TC, contactCounter.startDate, token);
    }
}
