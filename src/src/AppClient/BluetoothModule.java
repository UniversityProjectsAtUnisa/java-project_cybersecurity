package src.AppClient;

import java.util.*;

import utils.RandomUtils;

public class BluetoothModule {
    private static final Map<Integer, byte[]> codeMap = new HashMap<>();
    private static final Map<Integer, Set<Integer>> userMap = new HashMap<>();
    private static int numIndex = 1;

    private static final double MAX_BLUETOOTH_DISTANCE = 5.0;
    private final int userNum;

    public BluetoothModule() {
        this.userNum = numIndex++;
    }

    public void emit(byte[] code) {
        codeMap.put(userNum, code);
    }

    public BluetoothScan[] scan() {
        Set<Integer> closeUsers = userMap.get(userNum);
        if (closeUsers == null) return new BluetoothScan[0];
        BluetoothScan[] res = new BluetoothScan[closeUsers.size()];
        int i=0;
        for (Integer closeUser: closeUsers) {
            byte[] code = codeMap.get(closeUser);
            double distance = Math.random() * MAX_BLUETOOTH_DISTANCE;
            res[i++] = new BluetoothScan(code, distance);
        }
        return res;
    }

    //  SIMULATION METHODS

    /**
     * Allow simulation of user proximity
     */
    public static void populateRandomUserMap() {
        List<Integer> userNums = Arrays.asList(codeMap.keySet().toArray(Integer[]::new));

        int contactCount = codeMap.size() / 2;
        for (int i=0; i < contactCount; i++) {
            Integer first = RandomUtils.pickOne(userNums);
            Integer last = RandomUtils.pickOne(userNums);
            while (Objects.equals(last, first)) last = RandomUtils.pickOne(userNums);
            // CREATE MAPPING: FIRST USER --> LAST USER
            Set<Integer> firstList = userMap.computeIfAbsent(first, k -> new HashSet<>());
            firstList.add(last);
            // CREATE MAPPING: LAST USER --> FIRST USER
            Set<Integer> lastList = userMap.computeIfAbsent(last, k -> new HashSet<>());
            lastList.add(first);
        }
    }

    public static void clearSimulationData() {
        codeMap.clear();
        userMap.clear();
    }
}
