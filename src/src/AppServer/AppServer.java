package src.AppServer;

import java.io.IOException;
import java.security.*;
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
    private SecretKey keySigmaSwab;
    /*private byte[] saltPassword;*/
    private byte[] saltCode = new byte[32];

    private SecretKey keyToken;
    private SecretKey keyInfo;

    /*private byte[] keySigmaSwab = new byte[32];*/
    private SecretKey keySwab;
    Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

    /**
     * GENERATORsalt_pass è un csPRG che usa come seme di generazione
     * “SEEDpassword” per generare i SALTpassword(user). GENERATORswab è una
     * csPRG che usa come seme di generazione “SEEDswab” per generare i
     * SALTswab.
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
        this.keySigmaSwab = new SecretKeySpec(key1.getEncoded(), 0, key1.getEncoded().length, "AES");
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
            if (!endpointName.equals("login") && !endpointName.equals("register")) {
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
            }
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
                    data = isPositive(loggedUser);
                    break;
                case "sendPositiveData":
                    List<PositiveContact> codes = (LinkedList<PositiveContact>) req.getPayload();
                    data = this.sendPositiveSeed(loggedUser, codes);
                    break;
                case "getPositiveSeeds":
                    data = this.getPositiveSeed(loggedUser);
                    break;
                case "isAtRisk":
                    List<Seed> userSeeds = (LinkedList<Seed>) req.getPayload();
                    data = this.isAtRisk(loggedUser, userSeeds);
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
        byte[] cfBytes = cf.getBytes();
        byte[] hashedCf = ServerUtils.encryptWithSalt(cfBytes, this.saltCf);
        User user = this.database.findUser(hashedCf);
        if (user == null) {
            throw new AuthenticationException("Invalid credentials");
        }
        byte[] passwordBytes = password.getBytes();
        byte[] passwordSalt = user.getPasswordSalt(keyInfo);
        byte[] hashedPassword = ServerUtils.encryptWithSalt(passwordBytes, passwordSalt);

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

        byte[] cfHashed = ServerUtils.encryptWithSalt(cfBytes, saltCf);
        byte[] hashedPassword = ServerUtils.encryptWithSalt(passwordBytes, passwordSalt);

        if (!this.database.addUser(cfHashed, hashedPassword, passwordSalt, keyInfo)) {
            throw new InsertFailedException("User registration failed");
        }
        return true;
    }

    // FASE 3
    public boolean isPositive(User user) {
        return user.getIsPositive(keyInfo);
    }

    public boolean sendPositiveSeed(User user, List<PositiveContact> pcs) {
        // Remove duplicates
        Map<String, PositiveContact> uniquePcs = new HashMap<>();
        pcs.forEach(pc -> uniquePcs.put(Arrays.toString(pc.getSeed()), pc));

        for (PositiveContact plainPc : uniquePcs.values()) {
            // Encrypt every code pairs and database insert
            List<CodePair> encPairs = encryptCodePairs(plainPc.getDetectedCodes());
            boolean res = database.createPositiveContact(plainPc.getSeed(), plainPc.getSeedCreationDate(), encPairs);
            if (!res) throw new RuntimeException("Unable to create positive contact!");
        }
        // UPDATE USER: lastRiskRequestDate=Date.now(), hadRequestSeed=true, isPositive=false
        database.updateUser(user.getHashedCf(), null, null, ServerUtils.getNow(), true, false, keyInfo);
        return true;
    }

    // FASE 4
    public LinkedList<Seed> getPositiveSeed(User user) {
        // CHECK: lastRiskRequestDate, hadRequestSeed=false
        if (user.getHadRequestSeed(keyInfo))
            return null;
        else if (isDateInCurrentInterval(user.getLastRiskRequestDate(keyInfo)))
            return null;
        // FILTER SEEDS WHERE: data_generazione > max(data_corrente - 20 giorni,  data_minima_semi)
        long minSeedDate = user.getMinimumSeedDate(keyInfo);
        List<Seed> filtered = database
                .getAllPositiveSeeds()
                .stream()
                .filter(s -> {
                    long dMin = Math.max(minSeedDate, ServerUtils.getNow().getTime());
                    return s.getGenDate() > dMin / Config.TC;
                })
                .toList();
        // UPDATE USER: lastRiskRequestDate=Date.now(), hadRequestSeed=true
        database.updateUser(user.getHashedCf(), null, null, true, ServerUtils.getNow(), keyInfo);
        return new LinkedList<>(filtered);
    }

    public String isAtRisk(User user, List<Seed> userSeeds) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {
        // Encrypt every code pairs in the ContactMap
        int n = countValidContactReports(userSeeds);
        // If user is at risk: n * Tc > millis(15)
        if (n * Config.TC > 15 * 60 * 1000) {

            byte[] swabSalt = new byte[32];
            swabGenerator.nextBytes(swabSalt);

            byte[] swabCf = BytesUtils.concat(user.getHashedCf(), swabSalt);
            cipher.init(Cipher.ENCRYPT_MODE, keySwab);
            byte[] encryptedSwab = cipher.doFinal(swabCf);

            Mac hMac = Mac.getInstance("HMacSHA256");
            hMac.init(keySigmaSwab);
            hMac.update(encryptedSwab);
            byte[] hmac = hMac.doFinal();
            // byte[] hmac = ServerUtils.encryptWithSalt(saltSwab, ServerUtils.encryptWithSalt(saltSwab, swabCf));

            database.createSwab(encryptedSwab, ServerUtils.getNow());

            return BytesUtils.toString(encryptedSwab) + "_" + BytesUtils.toString(hmac);  // FIXME: SOSPETTO
        }

        // database.updateUser();

        return null;
    }

    // HA REQUESTS
    public boolean useNotification(String swab, String cf) {
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

    private List<CodePair> encryptCodePairs(List<CodePair> plainCodePairs) {
        return plainCodePairs
                .stream()
                .map(pair -> new CodePair(ServerUtils.encryptWithSalt(pair.getCode(), saltCode), pair.getInstant()))
                .toList();
    }

    private int countValidContactReports(List<Seed> seeds) {
        // PRE-CHECKS: no-duplicates, each seed has max Tseme / Tc instants, each seed is not a positive seed.
        HashMap<String, List<Long>> mappedPairs = new HashMap<>();
        for (Seed s: seeds) {
            List<Long> list = mappedPairs.computeIfAbsent(Arrays.toString(s.getValue()), k -> new LinkedList<>());
            list.add(s.getGenDate());
        }
        for (List<Long> instants: mappedPairs.values()) {
            // duplicate check
            if (new HashSet<>(instants).size() != instants.size())
                return 0;
            // max Tseme / Tc check
            if (instants.size() > Config.TSEME / Config.TC)
                return 0;
        }
        // check if user seeds are positive
        for (Seed seed: seeds) {
            if (database.findPositiveContact(seed.getValue()) != null)
                return 0;
        }
        // COUNT
        int count = 0;

        for (Seed seed : seeds) {
            long instant = seed.getGenDate();
            byte[] code = generateCode(seed.getValue(), instant);
            byte[] encryptedCode = ServerUtils.encryptWithSalt(code, saltCode);
            List<PositiveContact> pcs = database.findPositiveContactByCode(encryptedCode, instant);
            count += pcs.size();
        }

        return count;
    }

    private boolean isDateInCurrentInterval(long date) {
        long now = ServerUtils.getNow().getTime();
        long tStart = now - (now % Config.TSEME);
        return date >= tStart && date <= tStart + Config.TSEME;
    }

    private byte[] generateCode(byte[] seedValue, long instant) {
        byte[] concatenation = BytesUtils.concat(seedValue, BytesUtils.fromLong(instant));
        try {
            return MessageDigest.getInstance("SHA-256").digest(concatenation);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("NoSuchAlgorithmException(SHA-256) in AppServer(generateCode)");
    }
}
