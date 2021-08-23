package src.AppClient;

import apis.ServerApiService;
import core.tokens.AuthToken;
import utils.BytesUtils;
import utils.Credentials;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import utils.AppTimer;
import utils.Config;
import java.util.*;

public class AppClient {
    public enum AppClientState {
        NOT_LOGGED,
        LOGGED
    }

    private static final double MAX_DISTANCE = 4.0;

    private final ServerApiService serverApi = new ServerApiService();
    private final LocalStorage storage = new LocalStorage();
    private AppClientState appState = AppClientState.NOT_LOGGED;
    private final BluetoothModule ble = new BluetoothModule();
    private Seed seed;
    private List<CodePair> currentIntervalReceivedCodes;
    private AuthToken token;
    private Credentials tmpCredentials;

    public AppClient() {
        AppTimer.getInstance().subscribe(this);
        long instant = new Date().getTime() / Config.TC;
        startNewInterval(instant);
    }

    private Credentials getTmpCredentials() {
        if (tmpCredentials == null) tmpCredentials = FakeInput.getNextCredential();
        return tmpCredentials;
    }

    public boolean register() {
        return serverApi.register(getTmpCredentials());
    }

    public boolean login() {
        token = serverApi.login(getTmpCredentials());
        if (token == null)
            return false;
        appState = AppClientState.LOGGED;
        return true;
    }

    public void isUserPositive() {
        if (appState != AppClientState.LOGGED)
            throw new RuntimeException("User must be logged request: isUserPositive");
        // ASK SERVER IF USER IS POSITIVE
        boolean res = serverApi.isUserPositive(token);
        // SEND SEEDS, RECEIVED CODES WITH INSTANTS
        if (res) {
            HashMap<Seed, List<CodePair>> data = new HashMap<>();
            Map<Long, List<CodePair>> contactHistory = storage.getContactHistoryCopy();
            Map<Long, Seed> seedHistory = storage.getSeedHistoryCopy();
            seedHistory.keySet().forEach(l -> data.put(seedHistory.get(l), contactHistory.get(l)));
            serverApi.sendSeedsAndReceivedCodes(data, token);
        }
    }

    public void isUserAtRisk() throws NoSuchAlgorithmException {
        if (appState != AppClientState.LOGGED)
            throw new RuntimeException("User must be logged request: isUserAtRisk");
        // RETRIEVE FROM SERVER SEEDS OF POSITIVE USERS
        List<Seed> positiveSeeds = serverApi.getPositiveSeeds(token);
        // COMPUTE IF USER IS AT RISK
        LinkedList<Seed> pairs = findContactPairs(positiveSeeds);
        // SEND TO SERVER ALL PAIR FOUND
        if (pairs.size() * Config.TC >= 15 * 60 * 1000) {  // 15 * 60 * 1000 are millis in 15 minutes
            serverApi.reportContacts(pairs, token);
        }
    }

    public void startNewInterval(long intervalStart) {
        if (seed != null)
            storage.saveIntervalData(seed, currentIntervalReceivedCodes);
        // CREATE NEW SEED AND NEW BUCKET FOR RECEIVED CODES
        try {
            byte[] seedGen = new byte[256];
            SecureRandom r = SecureRandom.getInstanceStrong();
            r.nextBytes(seedGen);
            seed = new Seed(intervalStart, seedGen);
            currentIntervalReceivedCodes = new LinkedList<>();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void emit(long instant) throws NoSuchAlgorithmException {
        byte[] code = generateCode(seed.getValue(), instant);
        ble.emit(code);
    }

    public void scan(long instant) {
        BluetoothScan[] scanResult = ble.scan();
        Arrays.stream(scanResult)
                .filter((item) -> item.getDistance() < MAX_DISTANCE)
                .map(BluetoothScan::getCode)
                .forEach((code) -> {
                    CodePair pair = new CodePair(code, instant);
                    currentIntervalReceivedCodes.add(pair);
                });
    }

    private LinkedList<Seed> findContactPairs(List<Seed> positiveSeeds) throws NoSuchAlgorithmException {
        int tcInInterval = Config.TSEME / Config.TC;
        Set<Seed> contactPairs = new HashSet<>();
        Map<Long, List<CodePair>> contactHistory = storage.getContactHistoryCopy();
        Map<Long, Seed> seedHistory = storage.getSeedHistoryCopy();

        for (Seed positiveSeed: positiveSeeds) {
            long genDate = positiveSeed.getGenDate();
            byte[] userSeed = seedHistory.get(genDate).getValue();
            List<CodePair> codes = contactHistory.get(genDate);
            // FOR EACH INSTANT IN THE INTERVAL
            for (int i=0; i < tcInInterval; i++) {
                long instant = genDate + i * Config.TC;
                byte[] positiveCode = generateCode(positiveSeed.getValue(), instant);
                // SEARCH: CODE_POSITIVE == CODE_RECEIVED AND INSTANT_POSITIVE == INSTANT_RECEIVED
                for (CodePair receivedCode: codes) {
                    if (receivedCode.getInstant() == instant && Arrays.equals(receivedCode.getCode(), positiveCode)) {
                        contactPairs.add(new Seed(instant, userSeed));
                        break;
                    }
                }
            }
        }

        return new LinkedList<>(contactPairs);
    }

    private byte[] generateCode(byte[] seedValue, long instant) throws NoSuchAlgorithmException {
        byte[] concatenation = BytesUtils.concat(seedValue, BytesUtils.fromLong(instant));
        return MessageDigest.getInstance("SHA-256").digest(concatenation);
    }
}
