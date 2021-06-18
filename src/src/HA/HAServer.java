package src.HA;

import core.Request;
import core.Response;
import core.SSLServer;
import utils.Config;
import utils.SimulationData;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

public class HAServer extends SSLServer {
    public HAServer(String password) throws IOException {
        super(Config.HA_KEYSTORE, Config.CLIENT_TRUSTSTORE, password, Config.HA_SERVER_PORT, true);
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

    public boolean isCfValid(String cf) {
        return SimulationData.VALID_CF_LIST.contains(cf);
    }
}
