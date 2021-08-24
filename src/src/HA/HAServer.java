package src.HA;

import apis.RestrictedServerApiService;
import core.Request;
import core.Response;
import core.SSLServer;
import utils.Config;
import utils.SimulationData;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import utils.RandomUtils;
import utils.UserNotification;

public class HAServer {

    private final RestrictedServerApiService restrictedServerApiService;
    private final SSLServer publicServer, restrictedServer;

    private final Set<String> positiveUsers = new HashSet<>();

    public HAServer(String password) throws IOException {
        publicServer = new SSLServer(Config.HA_KEYSTORE, Config.CLIENT_TRUSTSTORE, password, Config.PUBLIC_HA_SERVER_PORT) {
            @Override
            protected Response handleRequest(Request req) {
                return publicHandleRequest(req);
            }
        };
        restrictedServer = new SSLServer(Config.HA_KEYSTORE, Config.CLIENT_TRUSTSTORE, password, Config.HA_SERVER_PORT, true) {
            @Override
            protected Response handleRequest(Request req) {
                return restrictedHandleRequest(req);
            }
        };
        restrictedServerApiService = new RestrictedServerApiService();
    }

    private Response restrictedHandleRequest(Request req) {
        String endpointName = req.getEndpointName();
        try {
            switch (endpointName) {
                case "CF_IS_VALID":
                    boolean payload = isCfValid((String) req.getPayload());
                    return Response.make(payload);
            }
            return Response.make("Endpoint not valid!");
        } catch (Exception e) {
            Logger.getGlobal().warning(endpointName + ' ' + e.getMessage());
            return Response.error("Internal server error");
        }
    }

    private Response publicHandleRequest(Request req) {
        String endpointName = req.getEndpointName();
        try {
            switch (endpointName) {
                case "USE_NOTIFICATION":
                    boolean res = useNotification((UserNotification) req.getPayload());
                    return Response.make(res);
            }
            return Response.make("Endpoint not valid!");
        } catch (Exception e) {
            Logger.getGlobal().warning(endpointName + ' ' + e.getMessage());
            return Response.error("Internal server error");
        }
    }

    public void start() {
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    notifyPositiveUser();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Timer tm = new Timer();
        tm.schedule(task, Config.NEW_POSITIVE_INTERVAL * 5, Config.NEW_POSITIVE_INTERVAL);
        new Thread(restrictedServer::start).start();
        publicServer.start();
    }

    public void notifyPositiveUser() {
        String cf = RandomUtils.pickOne(SimulationData.VALID_CF_LIST);
        if (positiveUsers.add(cf)) {
            Logger.getGlobal().info(String.format("New positive user: %s", cf));
            restrictedServerApiService.notifyPositiveUser(cf);
        }
    }

    public boolean useNotification(UserNotification notice) {
        Logger.getGlobal().info(String.format("User(%s) is using a swab", notice.getCf()));
        return restrictedServerApiService.useNotification(notice);
    }

    public boolean isCfValid(String cf) {
        return SimulationData.VALID_CF_LIST.contains(cf);
    }
}
