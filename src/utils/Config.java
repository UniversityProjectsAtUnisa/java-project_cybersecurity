package utils;

public class Config {
    public static final int CLIENT_COUNT = 10;
    public static final int EXPIRE_DAYS = 5;
    public static final int RISK_MINUTES = 5;

    public static final int TC = 3000;
    public static final int N_CUM = 30;

    public static final String APP_SERVER_IP = "localhost";
    public static final int APP_SERVER_PORT = 7007;
    public static final int RESTRICTED_APP_SERVER_PORT = 7008;

    public static final String HA_SERVER_IP = "localhost";
    public static final int HA_SERVER_PORT = 5763;
    
    public static final String KEYSTORES_BASE_PATH = "./src/core/keys/";
    public static final String HA_KEYSTORE = "client/HAKeystore.jks";
    public static final String CLIENT_TRUSTSTORE = "client/clientTruststore.jks";
    public static final String SERVER_KEYSTORE = "server/serverKeystore.jks";
    public static final String SERVER_TRUSTSTORE = "server/serverTruststore.jks";
}
