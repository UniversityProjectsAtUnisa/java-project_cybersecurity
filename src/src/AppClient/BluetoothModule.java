package src.AppClient;

import utils.Config;

import java.util.HashSet;
import java.util.Set;

public class BluetoothModule {
    private static final double MAX_BLUETOOTH_DISTANCE = 12.0;
    private static final int MAX_SCAN_AMOUNT = 3;

    /**
     * Simulated non blocking bluetooth broadcast
     */
    public void emit() { }

    /**
     * Simulated blocking bluetooth broadcast
     */
    public BluetoothScan[] scan(int excludeId) {  // TODO: enhance logic
        int amt = (int) ((Math.random() * (MAX_SCAN_AMOUNT - 1)) + 1);
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
        while (res.size() < amount) {
            int newId = (int) ((Math.random() * (Config.CLIENT_COUNT - 1)) + 1);
            if (newId != exclude) {
                res.add(newId);
            }
        }
        return res.toArray(new Integer[amount]);
    }
}
