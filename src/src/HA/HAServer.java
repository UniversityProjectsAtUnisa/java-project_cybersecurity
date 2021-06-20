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
            e.printStackTrace();
            String log = "Server Internal Error: " + e.getMessage();
            Logger.getGlobal().warning(log);
            return Response.error(log);
        }
    }

    @Override
    public void start() {
        TimerTask task1 = new TimerTask() {
            public void run() {
                notifyPositiveUser();
            }
        };
        Timer tm1 = new Timer();
        tm1.schedule(task1, 1000, 1000);

        TimerTask task2 = new TimerTask() {
            public void run() {
                useNotification();
            }
        };
        Timer tm2 = new Timer();
        tm2.schedule(task2, 1000, 1000);

        super.start();
    }

    public void notifyPositiveUser() {
        String cf = SimulationData.VALID_CF_LIST.get(0);  // TODO: add logic (random)
        restrictedServerApiService.notifyPositiveUser(cf);
    }

    public void useNotification() {
        String cf = SimulationData.VALID_CF_LIST.get(0);  // TODO: add logic
        restrictedServerApiService.useNotification("123456789", cf);
    }

    public boolean isCfValid(String cf) {
        return SimulationData.VALID_CF_LIST.contains(cf);
    }
}
