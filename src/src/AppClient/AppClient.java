package src.AppClient;

import apis.ServerApiService;
import utils.AppTimer;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class AppClient {
    private static final double MAX_DISTANCE = 2.0;

    private static class ContactCounter {
        private int count = 0;
        private final LocalDateTime startData;

        public ContactCounter() {
            startData = LocalDateTime.now();
        }

        public int getCount() {
            return count;
        }

        public LocalDateTime getStartData() {
            return startData;
        }

        public void increment() {
            count++;
        }
    }

    private AppClientState appState = AppClientState.NOT_LOGGED;
    private final BluetoothModule ble = new BluetoothModule();
    private final Map<Integer, ContactCounter> userCounter = new HashMap<>();
    private final ServerApiService serverApi = new ServerApiService();
    // TODO: private AuthToken token;
    private final Timer notificationTimer = new Timer();

    public boolean register() {
        Credentials c = FakeInput.getNextCredential();
        // TODO: success = serverApi.register(c.cf, c.password);
        return true;
    }

    public boolean login() {
        Credentials c = FakeInput.getNextCredential();
        // TODO: token = serverApi.login(c.cf, c.password);
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
        // TODO: serverApi.getNotifications(token);
    }

    public void scanAndEmit() {
        if (appState != AppClientState.LOGGED) throw new RuntimeException();
        ble.emit(); // non blocking
        BluetoothScan[] scanResult = ble.scan(1);  // TODO: replace 1 with token.id
        scanResult = Arrays.stream(scanResult).filter((item) -> item.getDistance() < MAX_DISTANCE).toArray(BluetoothScan[]::new);
        evaluateContactEnded(scanResult);
        for (BluetoothScan scan: scanResult) {
            evaluateScan(scan);
        }
    }

    private void evaluateScan(BluetoothScan scan) {
        int id = scan.getId();
        if (userCounter.containsKey(id)) {
            userCounter.get(id).increment();
        } else {
            userCounter.put(id, new ContactCounter());
        }
    }

    private void evaluateContactEnded(BluetoothScan[] scanResult) {
        Set<Integer> currentIds = new HashSet<>(userCounter.keySet());

        Set<Integer> scannedIds = new HashSet<>();
        for (BluetoothScan scan: scanResult) {
            scannedIds.add(scan.getId());
        }

        currentIds.removeAll(scannedIds);

        for (int id: currentIds) {
            ContactCounter contactCounter = userCounter.remove(id);
            Logger.getGlobal().info(String.format("ReportContact(id=%d, count=%d, startDate=%s)", id, contactCounter.getCount(), contactCounter.getStartData()));
            // TODO: serverApi.reportContact(id, contactCounter.getCount() * Config.TC, contactCounter.getStartData(), token);
        }
    }
}
