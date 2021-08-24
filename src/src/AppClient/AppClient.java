package src.AppClient;

import apis.PublicHaApiService;
import apis.ServerApiService;
import core.tokens.AuthToken;
import entities.PositiveContact;
import src.AppServer.ServerUtils;
import utils.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

public class AppClient {
    public enum AppClientState {
        NOT_LOGGED,
        LOGGED
    }

    private static final double MAX_DISTANCE = 4.0;

    private final ServerApiService serverApi = new ServerApiService();
    private final PublicHaApiService haApi = new PublicHaApiService();
    private final LocalStorage storage = new LocalStorage();
    private AppClientState appState = AppClientState.NOT_LOGGED;
    private final BluetoothModule ble = new BluetoothModule();
    private Seed seed;
    private List<CodePair> currentIntervalReceivedCodes;
    private AuthToken token;
    private Credentials tmpCredentials;

    public AppClient() {
        AppTimer.getInstance().subscribe(this);
        long instant = ServerUtils.getNow().getTime() / Config.TC;
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

    public void intervalRoutine() {
        boolean isPositive = isUserPositive();
        if (!isPositive) {
            try {
                isUserAtRisk();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isUserPositive() {
        if (appState != AppClientState.LOGGED) login();
        // ASK SERVER IF USER IS POSITIVE
        boolean res = serverApi.isUserPositive(token);
        // SEND SEEDS, RECEIVED CODES WITH INSTANTS
        if (res) {
            LinkedList<PositiveContact> data = new LinkedList<>();
            Map<Long, List<CodePair>> contactHistory = storage.getContactHistoryCopy();
            Map<Long, Seed> seedHistory = storage.getSeedHistoryCopy();
            seedHistory.keySet().forEach(l -> data.add(new PositiveContact(seedHistory.get(l).getValue(), l, contactHistory.get(l))));
            boolean sendRes = serverApi.sendSeedsAndReceivedCodes(data, token);
            if (sendRes) storage.clear();
            return sendRes;
        }
        return false;
    }

    public void isUserAtRisk() throws NoSuchAlgorithmException {
        if (appState != AppClientState.LOGGED) login();
        // RETRIEVE FROM SERVER SEEDS OF POSITIVE USERS
        List<Seed> positiveSeeds = serverApi.getPositiveSeeds(token);
        // COMPUTE IF USER IS AT RISK
        if (positiveSeeds != null) {
            LinkedList<Seed> pairs = findContactPairs(positiveSeeds);
            // SEND TO SERVER ALL PAIR FOUND
            Logger.getGlobal().info(String.format("User(%s) has %d risk time", tmpCredentials.getCf(), pairs.size() * Config.TC));
            if (pairs.size() * Config.TC >= Config.RISK_TIME) {
                byte[] notification = serverApi.reportContacts(pairs, token);
                if (notification != null) {
                    storage.clear();
                    Logger.getGlobal().info(String.format("User(%s) is at risk!", tmpCredentials.getCf()));
                    TimerTask task = new TimerTask() {
                        public void run() {
                            useNotification(notification);
                        }
                    };
                    new Timer().schedule(task, 1500);
                }
            }
        }
    }

    public void useNotification(byte[] notification) {
        UserNotification un = new UserNotification(notification, tmpCredentials.getCf());
        Logger.getGlobal().info(String.format("User(%s) is using notification token", tmpCredentials.getCf()));
        boolean isSwabUsed = haApi.useNotification(un);
        Logger.getGlobal().info(String.format(
                "User(%s) %s il tampone gratuito",
                tmpCredentials.getCf(),
                isSwabUsed ? "ha usato con successo" : "non è riuscito ad usare"
        ));
    }

    public void startNewInterval(long intervalStart) {
        if (seed != null)
            storage.saveIntervalData(seed, currentIntervalReceivedCodes);
        // CREATE NEW SEED AND NEW BUCKET FOR RECEIVED CODES
        try {
            byte[] seedGen = new byte[32];
            SecureRandom r = SecureRandom.getInstanceStrong();
            r.nextBytes(seedGen);
            seed = new Seed(intervalStart, seedGen);
            currentIntervalReceivedCodes = new LinkedList<>();
            // storage.printHistory();
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
        int tcCountInInterval = Config.TSEME / Config.TC;
        Set<Seed> contactPairs = new HashSet<>();
        Map<Long, List<CodePair>> contactHistory = storage.getContactHistoryCopy();
        Map<Long, Seed> seedHistory = storage.getSeedHistoryCopy();

        for (Seed positiveSeed: positiveSeeds) {
            long genDate = positiveSeed.getGenDate();
            if (seedHistory.containsKey(genDate)) {
                byte[] userSeed = seedHistory.get(genDate).getValue();
                List<CodePair> codes = contactHistory.get(genDate);
                // FOR EACH INSTANT IN THE INTERVAL
                for (int i=0; i < tcCountInInterval; i++) {
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
        }

        return new LinkedList<>(contactPairs);
    }

    private byte[] generateCode(byte[] seedValue, long instant) throws NoSuchAlgorithmException {
        byte[] concatenation = BytesUtils.concat(seedValue, BytesUtils.fromLong(instant));
        return MessageDigest.getInstance("SHA-256").digest(concatenation);
    }
}
