package src.AppServer;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class InitSecretVariables {
    private static String[] aliasArray = {
            "saltCf",
            "seedPassword",
            "seedToken",
            "saltToken",
            "saltSwab",
            "saltCode",
            "keyToken",
            "keySwab",
            "keyInfo"
    };

    public static void main(String[] args) throws Exception {
        for (String alias: aliasArray) {
            byte[] rawKey = new byte[32];
            SecureRandom r = SecureRandom.getInstanceStrong();
            r.nextBytes(rawKey);
            SecretKey key = new SecretKeySpec(rawKey, "AES");
            ServerUtils.storeToKeyStore(key,"changeit","./salts_keystore.jks",alias);
        }
    }
}
