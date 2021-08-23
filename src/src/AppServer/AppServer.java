package src.AppServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.sql.Timestamp;

import java.util.logging.Logger;

import apis.HAApiService;
import core.Request;
import core.Response;
import core.SSLServer;
import core.tokens.*;
import entities.*;
import exceptions.*;

import java.io.Serializable;

import src.AppClient.CodePair;
import src.AppClient.Seed;
import utils.*;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.AuthenticationException;

/**
 * Endpoints: Login login({codice_fiscale, password}) -> token Registrazione
 * register({codice_fiscale, password}) -> boolean Segnalazione contatto
 * createReport({id2, duration, data}, token) -> boolean Richiesta notifiche
 * getNotifications(token) -> Notification[] Verifica e Disabilita codice
 * tampone gratuito useNotification({code}) -> boolean Richiesta informazioni
 * codice tampone gratuito getNotificationDetails({code}, token) ->
 * NotificationDetail Tampone con risultato positivo dall’HA
 * notifyPositiveUser({codice_fiscale}) -> boolean
 */
public class AppServer {

/*    private String salt1 = "";
    private String salt2 = "";*/

    private byte[] saltCf = new byte[32];
    private byte[] seedPassword = new byte[32];
    private byte[] seedToken = new byte[32];
    private byte[] saltToken = new byte[32];
    private byte[] saltSwab = new byte[32];
    /*private byte[] saltPassword;*/
    private byte[] saltCode = new byte[32];

    private SecretKey keyToken;
    private SecretKey keyInfo;

    /*private byte[] keySigmaSwab = new byte[32];*/
    private SecretKey keySwab;
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");


    /**
     * GENERATORsalt_pass è un csPRG che usa come seme di generazione “SEEDpassword” per generare i SALTpassword(user).
     * GENERATORswab è una csPRG che usa come seme di generazione “SEEDswab” per generare i SALTswab.
     */
    private SecureRandom saltPasswordGenerator = new SecureRandom(seedPassword);
    private SecureRandom swabGenerator = new SecureRandom(seedToken);

    /*
    * SALTcf è una stringa di 256 bit puramente casuale.
    KEYtoken e KEYinfo sono stringhe di 256 bit puramente casuali.
    SEEDpassword è una stringa di 256 bit puramente casuale.
    SEEDswab è una stringa di 256 bit puramente casuale.
    SALTtoken è una stringa di 256 bit puramente casuale.
    SALTswab è una stringa pseudo-casuale di 256 bit, relativa a SWAB, generata tramite GENERATORswab.
    SALTpassword(user) è una stringa pseudo-casuale di 256 bit, relativa a user, generata tramite GENERATORsalt_pass.
    SALTcode è una stringa di 256 bit puramente casuali.
    KEYsigma_swab è una stringa di 256 bit puramente casuali
    * */

    private final Database database;
    private final HAApiService healthApiService;
    private final SSLServer publicServer, restrictedServer;
    private AuthToken token;

    public AppServer(String password) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException {
        publicServer = new SSLServer(Config.SERVER_KEYSTORE, Config.SERVER_TRUSTSTORE, password, Config.APP_SERVER_PORT) {
            @Override
            protected Response handleRequest(Request req) {
                return publicHandleRequest(req);
            }
        };
        restrictedServer = new SSLServer(Config.SERVER_KEYSTORE, Config.SERVER_TRUSTSTORE, password, Config.RESTRICTED_APP_SERVER_PORT, true) {
            @Override
            protected Response handleRequest(Request req) {
                return restrictedHandleRequest(req);
            }
        };
        healthApiService = new HAApiService();
        this.database = new Database();

        SecretKey key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "saltCf");
        this.saltCf = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "seedPassword");
        this.seedPassword = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "seedToken");
        this.seedToken = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "saltToken");
        this.saltToken = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "saltSwab");
        this.saltSwab = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "saltCode");
        this.saltCode = key1.getEncoded();


        key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "keyToken");
        this.keyToken = new SecretKeySpec(key1.getEncoded(), 0, key1.getEncoded().length, "AES");
        key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "keySwab");
        this.keySwab = new SecretKeySpec(key1.getEncoded(), 0, key1.getEncoded().length, "AES");
        key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "keyInfo");
        this.keyInfo = new SecretKeySpec(key1.getEncoded(), 0, key1.getEncoded().length, "AES");
    }

    public void start() {
        new Thread(restrictedServer::start).start();
        publicServer.start();
    }

    public synchronized Response restrictedHandleRequest(Request req) {
        String endpointName = req.getEndpointName();
        Serializable data = "Internal server error";
        try {
            switch (endpointName) {
                case "USE_NOTIFICATION":
                    UseNotificationMessage notice = (UseNotificationMessage) req.getPayload();
                    data = useNotification(notice.getSwabCode(), notice.getCf());
                    break;
                case "NOTIFY_POSITIVE_USER":
                    String cf = (String) req.getPayload();
                    data = notifyPositiveUser(cf);
                    System.out.println("NOTIFY POSITIVE USER DATA: " + data);
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

    public synchronized Response publicHandleRequest(Request req) {
        // UseNotification è chiamato dall'HA
        // TUtte le chiamate sono autenticate tranne login e register
        // Nel server dell'HA c'è notifyPositiveUser e useNotification
        // Nomi endpoint tutti maiuscoli
        // Sistema verify di notificationToken (useNotification): il cf a disposizione è hashato

        String endpointName = req.getEndpointName();
        User loggedUser = null;
        try {
            /*if (!endpointName.equals("login") && !endpointName.equals("register")) {
                AuthToken token = req.getToken();
                if (token == null) {
                    throw new AuthenticationException("The authentication token is not valid");
                }
                loggedUser = this.database.findUser(token.getCfToken(keyToken));
                if (loggedUser == null) {
                    throw new AuthenticationException("User not found");
                }
                Timestamp lastLoginDate = loggedUser.getLastLoginDate();
                if (!token.isValid(lastLoginDate, keyToken, saltToken)) {
                    throw new AuthenticationException("The authentication token is not valid");
                }
            }*/
            AuthToken token;
            byte[] hashedCf;
            Logger.getGlobal().info("the endpoint is " + endpointName);
            Serializable data = "Internal server error";
            switch (endpointName) {
                case "login":
                    Credentials loginData = (Credentials) req.getPayload();
                    data = this.login(loginData.getCf(), loginData.getPassword());
                    break;
                case "register":
                    Credentials registerData = (Credentials) req.getPayload();
                    data = this.register(registerData.getCf(), registerData.getPassword());
                    break;
                case "isPositive":
                    token = req.getToken();
                    hashedCf = token.getCfToken(keyToken);
                    data = this.isPositive(hashedCf);
                    break;
                case "sendPositiveData":
                    PositiveContact[] codes = (PositiveContact[]) req.getPayload();
                    data = this.sendPositiveSeed(codes);
                    break;
                case "getPositiveSeeds":
                    data = this.getPositiveSeed();
                    break;
                case "isAtRisk":
                    HashMap<Seed, List<CodePair>> contactMap = (HashMap<Seed, List<CodePair>>) req.getPayload();
                    token = req.getToken();
                    hashedCf = token.getCfToken(keyToken);
                    data = this.isAtRisk(hashedCf, contactMap);
                    break;
            }
            return Response.make(data);
        } catch (AuthenticationException e) {
            e.printStackTrace();
            Logger.getGlobal().warning(endpointName + ' ' + e.getMessage());
            return Response.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getGlobal().warning(endpointName + ' ' + e.getMessage());
            return Response.error("Internal server error");
        }
    }

    public AuthToken login(String cf, String password) throws NoSuchAlgorithmException, InvalidKeyException, AuthenticationException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        byte[] cfBytes = cf.getBytes(StandardCharsets.UTF_8);
        byte[] hashedCf = ServerUtils.encryptWithSalt(cfBytes, this.saltCf);
        User user = this.database.findUser(hashedCf);
        if (user == null) {
            throw new AuthenticationException("Invalid credentials");
        }
        byte[] passwordBytes = password.getBytes();
        String info = user.getDecryptedInfo(keyInfo);
        String saltPassword = info.split("_")[0];
        byte[] hashedPassword = ServerUtils.encryptWithSalt(passwordBytes, saltPassword.getBytes(StandardCharsets.UTF_8));

        System.out.println(Arrays.toString(saltPassword.getBytes(StandardCharsets.UTF_8)));

        if (Arrays.equals(hashedPassword, user.getHashedPassword())) {
            Timestamp d = ServerUtils.getNow();
            AuthToken token = new AuthToken(hashedCf, keyToken, saltToken, d);
            this.database.updateUser(hashedCf, d, null, null, keyInfo);
            return token;
        }
        return null;
    }

    public boolean register(String cf, String password) throws NoSuchAlgorithmException, InsertFailedException {
        if (!healthApiService.checkCf(cf)) {
            return false;
        }
        byte[] cfBytes = cf.getBytes();
        byte[] passwordBytes = password.getBytes();
        byte[] passwordSalt = new byte[32];
        saltPasswordGenerator.nextBytes(passwordSalt);

        System.out.println(Arrays.toString(passwordSalt));

        byte[] cfHashed = ServerUtils.encryptWithSalt(cfBytes, saltCf);
        byte[] passwordHashed = ServerUtils.encryptWithSalt(passwordBytes, passwordSalt);

        if (!this.database.addUser(cfHashed, passwordHashed, passwordSalt, keyInfo)) {
            throw new InsertFailedException("User registration failed");
        }
        return true;
    }

    public LinkedList<Seed> getPositiveSeed(){
        LinkedList<Seed> positiveSeed = this.database.getAllPositiveSeeds();
        return positiveSeed;
    }

    public boolean isPositive(byte[] cfHashed) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        User u = this.database.findUser(cfHashed);
        cipher.init(Cipher.DECRYPT_MODE, keyInfo);
        byte[] decodedInfo = cipher.doFinal(u.getInfo());
        String[] info = decodedInfo.toString().split("_");
        return info[2] != "false";
    }

    public boolean sendPositiveSeed(PositiveContact[] pcs) {
        LinkedList<byte[]> insertedSeeds = new LinkedList<>();
        for (PositiveContact pc : pcs) {
            byte[] seed = pc.getSeed();
            if (!this.database.createPositiveContact(seed, pc.getSeedCreationDate(), pc.getDetectedCodes())) {
                for (byte[] s : insertedSeeds) {
                    this.database.removePositiveContact(s);
                }
                return false;
            }
            insertedSeeds.add(seed);
        }
        return true;
    }

    public String isAtRisk(byte[] hashedCf, HashMap<Seed, List<CodePair>> contactMap) throws NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        int count = 0;
        for (Seed seed : contactMap.keySet()) {
            if (this.database.findPositiveContact(seed.getValue()) != null) {
                count++;
            }
        }
        if (count >= 60) {
            byte[] swabSalt = new byte[32];
            swabGenerator.nextBytes(swabSalt);
            byte[] swabCf = ServerUtils.encryptWithSalt(hashedCf, saltSwab);
            cipher.init(Cipher.ENCRYPT_MODE, keySwab);
            byte[] encriptedSwab = cipher.doFinal(swabCf);
            byte[] hmac = ServerUtils.encryptWithSalt(swabSalt, ServerUtils.encryptWithSalt(swabSalt, swabCf));
            this.database.createSwab(encriptedSwab, ServerUtils.getNow(), false);
            return BytesUtils.toString(encriptedSwab) + "_" + BytesUtils.toString(hmac);
        }
        return null;
    }

    public boolean useNotification(String swab, String cf){
        // Verificare che il codice esista
        // Verificare che la persona a cui è assegnato è cf
        // Verificare che sigma sia valido

        String[] swabParts = swab.split("_");
        byte[] encryptedSWAB = swabParts[0].getBytes();
        byte[] hmac = swabParts[1].getBytes();

        return true;
    }

    public boolean notifyPositiveUser(String cf) {
        return true;
    }
}
