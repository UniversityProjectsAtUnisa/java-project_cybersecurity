package src.AppClient;

import utils.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import utils.RandomUtils;

public class BluetoothModule {
    private static final Map<Integer, Integer> contactMap = new HashMap<>();

    private static final double MAX_BLUETOOTH_DISTANCE = 3.0;
    private final int maxRandomUsers;

    public BluetoothModule() {
        maxRandomUsers = Math.min(10, Config.CLIENT_COUNT - 1);
    }

    /**
     * Simulated non blocking bluetooth broadcast
     */
    public void emit() {
    }

    /**
     * Simulated blocking bluetooth broadcast
     */
    public BluetoothScan[] scan(int excludeId) {  // TODO: enhance logic
        int amt = RandomUtils.randomIntFromInterval(1, maxRandomUsers);
        BluetoothScan[] res = new BluetoothScan[amt];
        Integer[] ids = getUniqueIds(amt, excludeId);
        for (int i = 0; i < amt; i++) {
            double distance = Math.random() * MAX_BLUETOOTH_DISTANCE;
            res[i] = new BluetoothScan(ids[i], distance);
        }
        return res;
    }

    private Integer[] getUniqueIds(int amount, int exclude) {
        Set<Integer> res = new HashSet<>();

        Integer other = contactMap.remove(exclude);
        if (other != null && Math.random() > 0.8) res.add(other);  // Pr[add other] = 0.8

        while (res.size() < amount) {
            int newId = RandomUtils.randomIntFromInterval(1, Config.CLIENT_COUNT);
            if (newId != exclude) {
                res.add(newId);
            }
        }

        Integer[] ids = res.toArray(Integer[]::new);
        contactMap.put(ids[0], exclude);  // enhance simulation
        return ids;
    }
}
