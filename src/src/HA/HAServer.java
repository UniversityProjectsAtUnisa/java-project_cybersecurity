package src.HA;

import apis.RestrictedServerApiService;
import core.Request;
import core.Response;
import core.SSLServer;
import utils.Config;
import utils.SimulationData;

import java.io.IOException;
import java.io.Serializable;
import java.lang.System.Logger.Level;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import utils.RandomUtils;
import utils.UseNotificationMessage;

public class HAServer extends SSLServer {

    private final LinkedList<String> availableNotificationTokens = new LinkedList<>();
    private final LinkedList<Integer> toBeNotifiesUserIds = new LinkedList<>();
    private final LinkedList<String> nextPositiveUsers = new LinkedList<>();
    private final RestrictedServerApiService restrictedServerApiService;

    public HAServer(String password) throws IOException {
        super(Config.HA_KEYSTORE, Config.CLIENT_TRUSTSTORE, password, Config.HA_SERVER_PORT, true);
        restrictedServerApiService = new RestrictedServerApiService();
    }

    @Override
    protected Response handleRequest(Request req) {
        String endpointName = req.getEndpointName();
        Serializable data = "Internal server error";
        try {
            switch (endpointName) {
                case "CF_IS_VALID":
                    boolean payload = isCfValid((String) req.getPayload());
                    return Response.make(payload);
                case "ADD_SWAB_CODES":
                    LinkedList<String> codes = (LinkedList<String>) req.getPayload();
                    data = this.addSwabCodes(codes);
                    break;
                case "SEND_USER_IDS":
                    LinkedList<Integer> userIds = (LinkedList<Integer>) req.getPayload();
                    data = this.addUserIds(userIds);
                    break;
            }
            return Response.make(data);
        } catch (Exception e) {
            Logger.getGlobal().warning(endpointName + ' ' + e.getMessage());
            return Response.error("Internal server error");
//        }
//            String cf = (String) req.getPayload();
//            boolean success = notifyPositiveUser(cf);
//            return Response.make(success);
//        } catch (Exception e) {
//            Logger.getGlobal().warning("Server Internal Error: " + e.getMessage());
//            return Response.error("Server Internal Error");
//        }
        }
    }

    @Override
    public void start() {
        TimerTask task1 = new TimerTask() {
            public void run() {
                try {
                    notifyPositiveUser();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Timer tm1 = new Timer();
        tm1.schedule(task1, 1000, 1000);

        TimerTask task2 = new TimerTask() {
            public void run() {
                try {
                    useNotification();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Timer tm2 = new Timer();
        tm2.schedule(task2, 1000, 1000);

        super.start();
    }

    public void notifyPositiveUser() {
        String cf;
        if (!toBeNotifiesUserIds.isEmpty()) {
            Integer id = RandomUtils.pickOne(toBeNotifiesUserIds);
            cf = "CF" + id;
        } else if (!nextPositiveUsers.isEmpty()) {
            cf = RandomUtils.pickOne(nextPositiveUsers);
        } else if (RandomUtils.randomIntFromInterval(0, 20) == 21) { // TODO: Change
            cf = RandomUtils.pickOne(SimulationData.VALID_CF_LIST);
        } else {
            return;
        }
        if (!restrictedServerApiService.notifyPositiveUser(cf)) {
            Logger.getGlobal().warning("Failed to notify positive user");
        } else {
            nextPositiveUsers.remove(cf);
            Logger.getGlobal().info("Successfully notify positive user");
        }
    }

    public void useNotification() {
        String testCf = RandomUtils.pickOne(SimulationData.VALID_CF_LIST);
        String testCode = RandomUtils.pickOne(availableNotificationTokens);
        if (testCf != null && testCode != null) {
            if (restrictedServerApiService.useNotification(testCode, testCf)) {
                availableNotificationTokens.remove(testCode);
                nextPositiveUsers.add(testCf);
                Logger.getGlobal().info("Successfully used notification " + testCode);
            }
        }
    }

    public boolean isCfValid(String cf) {
        return SimulationData.VALID_CF_LIST.contains(cf);
    }

    public boolean addSwabCodes(LinkedList<String> codes) {
        boolean result = this.availableNotificationTokens.addAll(codes);
        Logger.getGlobal().info("Added '" + codes.size() + "' swabCodes, now there are '" + availableNotificationTokens.size() + "' swabCodes");
        return result;
    }

    public boolean addUserIds(LinkedList<Integer> userIds) {
        boolean result = this.toBeNotifiesUserIds.addAll(userIds);
        Logger.getGlobal().info("Added '" + userIds.size() + "' userIds, now there are '" + toBeNotifiesUserIds.size() + "' userIds");
        return result;
    }

}
