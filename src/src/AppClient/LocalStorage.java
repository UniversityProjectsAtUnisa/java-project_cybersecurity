package src.AppClient;

import java.util.HashMap;
import java.util.List;

public class LocalStorage {
    private final HashMap<Long, Seed> seedHistory = new HashMap<>();
    private final HashMap<Long, List<CodePair>> contactHistory = new HashMap<>();

    public void saveIntervalData(Seed seed, List<CodePair> receivedCodes) {
        seedHistory.put(seed.getGenDate(), seed);
        contactHistory.put(seed.getGenDate(), receivedCodes);
    }

    public HashMap<Long, List<CodePair>> getContactHistoryCopy() {
        return new HashMap<>(contactHistory);
    }

    public HashMap<Long, Seed> getSeedHistoryCopy() {
        return new HashMap<>(seedHistory);
    }

    public void printHistory() {
        for (Long l: contactHistory.keySet()) {
            System.out.println(l + ": " + contactHistory.get(l).toString());
        }
    }
}
