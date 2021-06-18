package src.AppClient;

import utils.Config;

import java.util.HashSet;
import java.util.Set;

public class BluetoothModule {

    private static final double MAX_BLUETOOTH_DISTANCE = 12.0;
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
        int amt = randomIntFromInterval(1, maxRandomUsers);
        BluetoothScan[] res = new BluetoothScan[amt];
        Integer[] ids = getUniqueIds(amt, excludeId);
        for (int i = 0; i < amt; i++) {
            double distance = Math.random() * MAX_BLUETOOTH_DISTANCE;
            res[i] = new BluetoothScan(ids[i], distance);
        }
        return res;
    }

    private int randomIntFromInterval(int min, int max) { // min and max included 
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }

    private Integer[] getUniqueIds(int amount, int exclude) {
        Set<Integer> res = new HashSet<>();
        while (res.size() < amount) {
            int newId = randomIntFromInterval(1, Config.CLIENT_COUNT);
            if (newId != exclude) {
                res.add(newId);
            }
        }
        return res.toArray(new Integer[amount]);
    }
}
