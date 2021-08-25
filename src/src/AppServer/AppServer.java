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
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.AuthenticationException;

/**
 * USERS ENDPOINTS:
 *      - LOGIN: (codiceFiscale, password) -> Token di Autenticazione.
 *      - REGISTER: (codiceFiscale, password}) -> Esito registrazione.
 *      - IS_POSITIVE: (authToken) -> Vero se l'utente è positivo altrimenti Falso.
 *      - SEND_POSITIVE_DATA: (authToken, listaCoppieSemeIstante) -> Vero se l'operazione è terminata con successo altrimenti, Falso.
 *      - GET_POSITIVE_SEEDS: (authToken) -> Lista delle coppie (semePositivo, instanteGenerazioneSeme).
 *      - IS_AT_RISK: (authToken, listaCoppieSemeInstate) -> Il codice tampone gratuito se l'operazione termina con successo, altrimenti null.
 * HA ENDPOINTS:
 *      - USE_NOTIFICATION: (codiceTamponeGratuito, codiceFiscale) -> Vero se l'operazione termina con successo, altrimenti Falso.
 *      - NOTIFY_POSITIVE_USER: (codiceFiscale) -> Vero se l'operazione termina con successo, altrimenti Falso.
 */
public class AppServer {

    private static final String KEY_STORE = "./salts_keystore.jks";
    private static final String PASSWORD = "changeit";

    private final byte[] saltCf;
    private final byte[] seedPassword;
    private final byte[] seedToken;
    private final byte[] saltToken;
    private final byte[] saltCode;

    private final byte[] seedTokenIv;
    private final byte[] seedSwabIv;
    private final byte[] seedUserIv;

    private final SecretKey keyToken;
    private final SecretKey keyInfo;

    private final SecretKey keySigmaSwab;
    private final SecretKey keySwab;

    private final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

    /**
     * saltPasswordGenerator is a csPRG that uses: seedPassword
     * swabGenerator is a csPRG that uses: seedToken
     * tokenIvGenerator, swabIvGenerator, userIvGenerator are csPRG that generates IV per AES CBC
     */
    private final SecureRandom saltPasswordGenerator;
    private final SecureRandom swabGenerator;
    private final SecureRandom tokenIvGenerator;
    private final SecureRandom swabIvGenerator;
    private final SecureRandom userIvGenerator;

    /*
    SALTcf è una stringa di 256 bit puramente casuale.
    KEYtoken e KEYinfo sono stringhe di 256 bit puramente casuali.
    SEEDpassword è una stringa di 256 bit puramente casuale.
    SEEDswab è una stringa di 256 bit puramente casuale.
    SALTtoken è una stringa di 256 bit puramente casuale.
    SALTswab è una stringa pseudo-casuale di 256 bit, relativa a SWAB, generata tramite GENERATORswab.
    SALTpassword(user) è una stringa pseudo-casuale di 256 bit, relativa a user, generata tramite GENERATORsalt_pass.
    SALTcode è una stringa di 256 bit puramente casuali.
    KEYsigma_swab è una stringa di 256 bit puramente casuali
    */
    private final Database database;
    private final HAApiService healthApiService;
    private final SSLServer publicServer, restrictedServer;

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
        // KEYS AND SALTS INIT
        SecretKey key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "saltCf");
        this.saltCf = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "seedPassword");
        this.seedPassword = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "seedToken");
        this.seedToken = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "saltToken");
        this.saltToken = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "saltSwab");
        this.keySigmaSwab = new SecretKeySpec(key1.getEncoded(), 0, key1.getEncoded().length, "AES");
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "saltCode");
        this.saltCode = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "seedTokenIv");
        this.seedTokenIv = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "seedSwabIv");
        this.seedSwabIv = key1.getEncoded();
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "seedUserIv");
        this.seedUserIv = key1.getEncoded();

        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "keyToken");
        this.keyToken = new SecretKeySpec(key1.getEncoded(), 0, key1.getEncoded().length, "AES");
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "keySwab");
        this.keySwab = new SecretKeySpec(key1.getEncoded(), 0, key1.getEncoded().length, "AES");
        key1 = ServerUtils.loadFromKeyStore(KEY_STORE, PASSWORD, "keyInfo");
        this.keyInfo = new SecretKeySpec(key1.getEncoded(), 0, key1.getEncoded().length, "AES");
        // GENERATORS INIT
        saltPasswordGenerator = new SecureRandom(seedPassword);
        swabGenerator = new SecureRandom(seedToken);
        tokenIvGenerator = new SecureRandom(seedTokenIv);
        swabIvGenerator = new SecureRandom(seedSwabIv);
        userIvGenerator = new SecureRandom(seedUserIv);
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
                    UserNotification notice = (UserNotification) req.getPayload();
                    data = useNotification(notice.getFullSwab(), notice.getCf());
                    break;
                case "NOTIFY_POSITIVE_USER":
                    String cf = (String) req.getPayload();
                    data = notifyPositiveUser(cf);
                    break;
            }
            return Response.make(data);
        } catch (Exception e) {
            Logger.getGlobal().warning(endpointName + ' ' + e.getMessage());
            return Response.error("Internal server error");
        }
    }

    public synchronized Response publicHandleRequest(Request req) {
        String endpointName = req.getEndpointName();
        User loggedUser = null;
        try {
            // All request are authenticated except login and register
            if (!endpointName.equals("LOGIN") && !endpointName.equals("REGISTER")) {
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
            Logger.getGlobal().info("the endpoint is " + endpointName);
            Serializable data = "Internal server error";
            switch (endpointName) {
                case "LOGIN":
                    Credentials loginData = (Credentials) req.getPayload();
                    data = this.login(loginData.getCf(), loginData.getPassword());
                    break;
                case "REGISTER":
                    Credentials registerData = (Credentials) req.getPayload();
                    data = this.register(registerData.getCf(), registerData.getPassword());
                    break;
                case "IS_POSITIVE":
                    data = isPositive(loggedUser);
                    break;
                case "SEND_POSITIVE_DATA":
                    List<PositiveContact> codes = (LinkedList<PositiveContact>) req.getPayload();
                    data = sendPositiveSeed(loggedUser, codes);
                    break;
                case "GET_POSITIVE_SEEDS":
                    data = getPositiveSeed(loggedUser);
                    break;
                case "IS_AT_RISK":
                    List<Seed> userSeeds = (LinkedList<Seed>) req.getPayload();
                    data = isAtRisk(loggedUser, userSeeds);
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

    public AuthToken login(String cf, String password) throws NoSuchAlgorithmException, InvalidKeyException, AuthenticationException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
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
            AuthToken token = AuthToken.createToken(hashedCf, keyToken, saltToken, d, tokenIvGenerator);
            this.database.updateUser(hashedCf, d, null, null, keyInfo);
            return token;
        }
        return null;
    }

    public boolean register(String cf, String password) throws InsertFailedException {
        if (!healthApiService.checkCf(cf)) {
            return false;
        }
        byte[] cfBytes = cf.getBytes();
        byte[] passwordBytes = password.getBytes();
        byte[] passwordSalt = new byte[32];
        saltPasswordGenerator.nextBytes(passwordSalt);

        byte[] cfHashed = ServerUtils.encryptWithSalt(cfBytes, saltCf);
        byte[] hashedPassword = ServerUtils.encryptWithSalt(passwordBytes, passwordSalt);

        if (!database.addUser(cfHashed, hashedPassword, passwordSalt, keyInfo, userIvGenerator)) {
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
        // CHECK: lastRiskRequestDate not in current interval
        if (isDateInCurrentInterval(user.getLastRiskRequestDate(keyInfo)))
            return null;
        // FILTER SEEDS WHERE: data_generazione > max(data_corrente - 20 giorni,  data_minima_semi)
        long minSeedDate = user.getMinimumSeedDate(keyInfo);
        List<Seed> filtered = database
                .getAllPositiveSeeds()
                .stream()
                .filter(s -> {
                    long dMin = Math.max(minSeedDate, ServerUtils.getNow().getTime() - 20 * 24 * 60 * 60 * 1000);
                    return s.getGenDate() > dMin / Config.TC;
                })
                .toList();
        // UPDATE USER: lastRiskRequestDate=Date.now(), hadRequestSeed=true
        database.updateUser(user.getHashedCf(), null, null, true, ServerUtils.getNow(), keyInfo);
        return new LinkedList<>(filtered);
    }

    public byte[] isAtRisk(User user, List<Seed> userSeeds) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        // CHECK: lastRiskRequestDate in current interval, hadRequestSeed=true
        if (!user.getHadRequestSeed(keyInfo))
            return null;
        else if (!isDateInCurrentInterval(user.getLastRiskRequestDate(keyInfo)))
            return null;
        // UPDATE USER: hadRequestSeed=false
        database.updateUser(user.getHashedCf(), null, null, false, null, keyInfo);
        // Compute valid contact reports
        int n = countValidContactReports(userSeeds);
        // if user is at risk: n * Tc > millis(15)
        if (n * Config.TC > Config.RISK_TIME) {
            // GENERATE SWAB SALT
            byte[] swabSalt = new byte[32];
            swabGenerator.nextBytes(swabSalt);
            // GENERATE AND SAVE ENCRYPTED SWAB
            byte[] swabCf = BytesUtils.concat(user.getHashedCf(), swabSalt);
            byte[] iv = new byte[cipher.getBlockSize()];
            swabIvGenerator.nextBytes(iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySwab, new IvParameterSpec(iv));
            byte[] encryptedSwab = cipher.doFinal(swabCf);
            database.createSwab(encryptedSwab, ServerUtils.getNow());
            // GENERATE SWAB SIGMA
            byte[] hmac = computeSwabSigma(encryptedSwab);
            // UPDATE USER: set minimum_seed_date to current time
            database.updateUser(user, null, ServerUtils.getNow(), null, keyInfo);
            return BytesUtils.concat(iv, encryptedSwab, hmac);
        }
        return null;
    }

    // HA REQUESTS
    public boolean useNotification(byte[] fullSwab, String cf) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Logger.getGlobal().info(String.format("User(%s) is using a swab", cf));
        // CHECK SWAB SIGMA
        int splitPoint = fullSwab.length - 32;  // 32 is the length of hmac sigma
        byte[] iv = Arrays.copyOfRange(fullSwab, 0, cipher.getBlockSize());
        byte[] encryptedSWAB = Arrays.copyOfRange(fullSwab, cipher.getBlockSize(), splitPoint);
        byte[] userHmac = Arrays.copyOfRange(fullSwab, splitPoint, fullSwab.length);
        byte[] newHmac = computeSwabSigma(encryptedSWAB);
        if (!ServerUtils.secureByteCompare(userHmac, newHmac))
            return false;

        Swab dbSwab = database.findSwab(encryptedSWAB);
        // CHECK IF SWAB CODE IS IN THE DATABASE
        if (dbSwab == null)
            return false;
        // CHECK: SWAB.IS_USED=FALSE
        if (dbSwab.isUsed())
            return false;
        // CHECK: If the swab is valid in the current date
        // L'abbiamo commentato perché è troppo restrittiva e complica la simulazione
        /* if (ServerUtils.getNow().getTime() - dbSwab.getCreationDate().getTime() != Config.SWAB_DATE_OFFSET)
            return false; */
        // UPDATE DATABASE: set swab.is_used to true
        database.updateSwab(encryptedSWAB, true);
        // CHECK: CF
        cipher.init(Cipher.DECRYPT_MODE, keySwab, new IvParameterSpec(iv));
        byte[] decryptedSwab = cipher.doFinal(encryptedSWAB);
        byte[] hashedCf = Arrays.copyOfRange(decryptedSwab, 0, decryptedSwab.length - 32);
        if (!Arrays.equals(ServerUtils.encryptWithSalt(cf.getBytes(), saltCf), hashedCf))
            return false;
        // UPDATE USER: set minimum_seed_date to current time
        database.updateUser(hashedCf, null, ServerUtils.getNow(), null, keyInfo);
        return true;
    }

    public boolean notifyPositiveUser(String cf) {
        byte[] hashedCf = ServerUtils.encryptWithSalt(cf.getBytes(), saltCf);
        User user = database.findUser(hashedCf);
        if (user != null) {
            database.updateUser(hashedCf, null, ServerUtils.getNow(), null, true, keyInfo);
            Logger.getGlobal().info(String.format("New positive user: %s", cf));
        } else {
            Logger.getGlobal().warning("404: USER NOT FOUND -> notifyPositiveUser");
        }
        return true;
    }

    // UTILITY METHODS
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
        // System.out.format("%d - %d - %d\n", now, tStart, tStart + Config.TSEME);
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

    private byte[] computeSwabSigma(byte[] payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hMac = Mac.getInstance("HMacSHA256");
        hMac.init(keySigmaSwab);
        hMac.update(payload);
        return hMac.doFinal();
    }
}
