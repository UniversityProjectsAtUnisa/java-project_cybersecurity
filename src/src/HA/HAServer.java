package src.HA;

import apis.RestrictedServerApiService;
import core.Request;
import core.Response;
import core.SSLServer;
import utils.Config;
import utils.SimulationData;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class HAServer extends SSLServer {

    private final RestrictedServerApiService restrictedServerApiService;

    public HAServer(String password) throws IOException {
        super(Config.HA_KEYSTORE, Config.CLIENT_TRUSTSTORE, password, Config.HA_SERVER_PORT, true);
        restrictedServerApiService = new RestrictedServerApiService();
    }

    @Override
    protected Response handleRequest(Request req) {
        if (!req.getEndpointName().equals("CF_IS_VALID"))
            return Response.error("Invalid endpoint");
        try {
            boolean payload = isCfValid((String) req.getPayload());
            return Response.make(payload);
        } catch (Exception e) {
            String log = "Server Internal Error: " + e.getMessage();
            Logger.getGlobal().warning(log);
            return Response.error(log);
        }
    }

    @Override
    public void start() {
        TimerTask task = new TimerTask() {
            public void run() {
                notifyPositiveUser();
            }
        };
        Timer tm = new Timer();
        tm.schedule(task, 1000, 1000);
        super.start();
    }

    public void notifyPositiveUser() {
        String cf = SimulationData.VALID_CF_LIST.get(0);
        Logger.getGlobal().info("Sending POSITIVE NOTIFY to: " + cf);
        boolean success = restrictedServerApiService.notifyPositiveUser(cf);
        Logger.getGlobal().info("POSITIVE RESULT: " + success);
    }

    public boolean isCfValid(String cf) {
        return SimulationData.VALID_CF_LIST.contains(cf);
    }
}
