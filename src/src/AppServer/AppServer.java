package src.AppServer;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.*;
import java.sql.Timestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

import apis.HAApiService;
import core.Request;
import core.Response;
import core.SSLServer;
import core.tokens.*;
import entities.Contact;
import entities.ContactReport;
import entities.User;
import entities.Notification;
import java.io.Serializable;
import java.util.logging.Level;
import utils.Config;

import javax.crypto.SecretKey;
import javax.naming.AuthenticationException;
import javax.xml.crypto.Data;
import utils.ContactReportMessage;
import utils.Credentials;
import java.text.MessageFormat;

/**
 *
 * Endpoints: Login login({codice_fiscale, password}) -> token Registrazione
 * register({codice_fiscale, password}) -> boolean Segnalazione contatto
 * createReport({id2, duration, data}, token) -> boolean Richiesta notifiche
 * getNotifications(token) -> Notification[] Verifica e Disabilita codice
 * tampone gratuito useNotification({code}) -> boolean Richiesta informazioni
 * codice tampone gratuito getNotificationDetails({code}, token) ->
 * NotificationDetail Tampone con risultato positivo dall’HA
 * notifyPositiveUser({codice_fiscale}) -> boolean
 *
 */
public class AppServer extends SSLServer {

    private String salt1 = "";
    private String salt2 = "";
    private final Database database;
    private final HAApiService healthApiService;

    public AppServer(String password) throws IOException {
        super(Config.SERVER_KEYSTORE, Config.SERVER_TRUSTSTORE, password, Config.APP_SERVER_PORT);
        healthApiService = new HAApiService(password);
        this.database = new Database();
        SecretKey key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "salt1");
        this.salt1 = ServerUtils.toString(key1.getEncoded());
        SecretKey key2 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "salt2");
        this.salt2 = ServerUtils.toString(key2.getEncoded());
    }

    public Response handleRequest(Request req) {
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
                Timestamp lastLoginDate = this.database.findUser(token.getId()).getLastLoginDate();
                if (!token.isValid(lastLoginDate, this.getSalt2())) {
                    throw new AuthenticationException("The authentication token is not valid");
                }
                loggedUser = this.database.findUser(token.getId());
            }

            Logger.getGlobal().info(endpointName);
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
                case "createReport":
                    ContactReportMessage createReportData = (ContactReportMessage) req.getPayload();
                    data = this.createReport(createReportData.getIdUserToReport(), createReportData.getDuration(), createReportData.getStartDateTime(), loggedUser);
                    break;
                case "getNotifications":
                    data = this.getNotifications(loggedUser);
                    break;
                case "getNotificationSuspensionDate":
                    String code = (String) req.getPayload();
                    data = this.getNotificationSuspensionDate(code, loggedUser);
                    break;

//                case "notifyPositiveUser":
//                    response = this.notifyPositiveUser(payload.getCf());
//                    break;
//                case "useNotification":
//                    response = this.useNotification(payload.getCode());
//                    break;
            }
            return Response.make(data);
        } catch (AuthenticationException e) {
            Logger.getGlobal().warning(e.getMessage());
            return Response.error(e.getMessage());
        } catch (Exception e) {
            Logger.getGlobal().warning(e.getMessage());
            return Response.error("Internal server error");
        }
    }

    public AuthToken login(String cf, String password) throws NoSuchAlgorithmException, InvalidKeyException {
        User user = this.database.findUser(ServerUtils.toByteArray(cf));
        if (user == null) {
            return null;
        }
        byte[] userSalt = user.getUserSalt();
        byte[] passwordBytes = password.getBytes();
        byte[] passwordConcat = ServerUtils.concatByteArray(passwordBytes, userSalt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHashed = md.digest(passwordConcat);
        if (Arrays.equals(passwordHashed, user.getPassword())) {
            int id = user.getId();
            AuthToken token = new AuthToken(id, this.getSalt2());
            this.database.updateUser(user.getCf(), token.getCreatedAt(), null, null);
            return token;
        }
        return null;
    }

    public boolean register(String cf, String password) throws NoSuchAlgorithmException {
        if (!healthApiService.checkCf(cf)){
            return false;
        }
        byte[] passwordBytes = password.getBytes();
        byte[] userSalt = new byte[32];
        new SecureRandom().nextBytes(userSalt);
        byte[] passwordConcat = ServerUtils.concatByteArray(passwordBytes, userSalt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHashed = md.digest(passwordConcat);

        this.database.addUser(ServerUtils.toByteArray(cf), passwordHashed, userSalt);

        return true;
    }

    public boolean createReport(int id, int duration, Timestamp date, User loggedUser) throws NoSuchAlgorithmException, InvalidKeyException {
        System.out.println("CONTATTI NEL DB: " + this.database.contacts.size());
        System.out.println("REPORTS NEL DB: " + this.database.contactReports.size());
        byte[] cfReporter = loggedUser.getCf();
        byte[] cfReported = this.database.findUser(id).getCf();

        // FASE 1: Ricerca report nel database
        // FASE 2: Individuare sovrapposizioni
        // FASE 2.1: Creare i contatti per le sovrapposizioni
        // FASE 3: Creare il nuovo report se è il più recente
        // FASE 4: Cancellare i vecchi report
        //        
        //
        // FASE 1
        // Se il reporter sono io e il reported è la persona che ho appena visto
        // Certo tutti i report in cui il reported ha visto il reporter
        // Ovvero tutti i report in cui la persona che ho appena visto ha visto me
        List<ContactReport> reports = this.database.searchContactReportsOfUsers(cfReported, cfReporter);

        // FASE 2
        // Individuo tutte le sovrapposizioni tra il report attuale ed i report
        // Individuati nella FASE 1
        ContactReport newReport = new ContactReport(cfReporter, cfReported, duration, date);
        List<ContactReport> overlaps = reports.stream().map(report -> report.findOverlapWith(newReport)).filter(report -> report != null).toList();

        // FASE 2.1
        // Per ogni sovrapposizione così individuata creo un nuovo contatto e
        // lo inserisco nel database.
        overlaps.forEach(this.database::addContact);
                
        // FASE 3
        // Vedo se esiste un report più recente di quello che sto per creare.
        // Un report più recente è un report che finisce dopo.
        // Se NON esiste un report più recente inserisco il report corrente nel database.
        ContactReport mostRecentReport = newReport;
        for (ContactReport r : reports) {
            if (r.getEndDate().after(mostRecentReport.getEndDate())) {
                mostRecentReport = r;
            }
        }

        boolean anyReportIsMoreRecent = mostRecentReport.getEndDate().after(newReport.getEndDate());
        if (!anyReportIsMoreRecent) {
            this.database.addContactReport(newReport);
        }

        // FASE 4
        // Cancello tutti i report tranne il più recente
        for (ContactReport r : reports) {
            if (!r.equals(mostRecentReport)) {
                this.database.removeContactReport(r.getReporterId(), r.getReportedId(), r.getStartDate());
            }
        }

        return true;

//        System.out.println("Reports found " + reports.size());
//
//        Timestamp maxContactEndDate = new Timestamp(0);
//
//        Timestamp timestampReportEndDate = ServerUtils.addMillis(date, duration);
//        Timestamp timestampAfterSubtraction = ServerUtils.minusMillis(ServerUtils.getNow(), 15 * 60);
//        // algorithm
//        for (ContactReport c : reports) {
//
//            Timestamp timestampCEndDate = ServerUtils.addMillis(c.getStartContactDate(), c.getDuration());
//            maxContactEndDate = ServerUtils.maxTimestamp(maxContactEndDate, timestampCEndDate);
//
//            if ((!c.getStartContactDate().after(date) && !date.after(timestampCEndDate)
//                    || !c.getStartContactDate().before(date) && !timestampReportEndDate.before(c.getStartContactDate()))
//                    && !c.getStartContactDate().before(timestampAfterSubtraction)) {
//
//                if (timestampReportEndDate.after(timestampCEndDate)) {
//                    this.database.removeContactReport(cfReported, cfReporter, c.getStartContactDate());
//                }
//
//                Timestamp maxInit = ServerUtils.maxTimestamp(date, c.getStartContactDate());
//                Timestamp minEnd = ServerUtils.minTimestamp(timestampReportEndDate, timestampCEndDate);
//                int durationNewContact = ServerUtils.diffTimestampSec(minEnd, maxInit);
//
//                System.out.println("CREAZIONE CONTATTO");
//                Logger.getGlobal().log(Level.INFO, MessageFormat.format("Contact(cfReporter={0}, cfReported={1}, durationNewContact={2}, maxInit={3})", cfReporter, cfReported, durationNewContact, maxInit));
//                this.database.addContact(cfReporter, cfReported, durationNewContact, maxInit);
//
//            }
//        }
//        if (timestampReportEndDate.after(maxContactEndDate)) {
//            System.out.println("CI ENTRA");
//            this.database.addContactReport(cfReporter, cfReported, duration, date);
//        }
//
//        return true;
    }

    public LinkedList<String> getNotifications(User loggedUser) throws NoSuchAlgorithmException, InvalidKeyException {
        List<Notification> dbTokens = this.database.searchUserNotifications(loggedUser.getId());
        LinkedList<String> tokens = new LinkedList<>();

        for (Notification dbToken : dbTokens) {
            tokens.add(dbToken.getCode());
        }

        return tokens;
    }

    public boolean useNotification(String code) {
        Notification notification = this.database.searchNotification(code);
        if (notification == null) {
            return false;
        }
        return this.database.updateNotification(code, ServerUtils.getNow()) != null;
    }

    public Timestamp getNotificationSuspensionDate(String code, User loggedUser) throws AuthenticationException {
        Notification notification = this.database.searchNotification(code);
        if (notification.getId() != loggedUser.getId()) {
            throw new AuthenticationException("The logged user does not own the selected notificationToken");
        }
        return notification.getSuspensionDate();
    }

    public boolean notifyPositiveUser(String cf) throws NoSuchAlgorithmException, InvalidKeyException {
        User user = this.database.findUser(ServerUtils.toByteArray(cf));
        List<Contact> contacts = this.database.searchContactsOfUser(user.getCf());
        user.setLastPositiveSwabDate(ServerUtils.getNow());
        this.database.updateUser(user.getCf(), null, null, ServerUtils.getNow());

        //algorithm
        Map<User, Integer> durationMap = new HashMap<>();
        for (Contact c : contacts) {
            User user_i = this.database.findUser(c.getReporterId());
            User user_j = this.database.findUser(c.getReportedId());
            LocalDateTime datetime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");

            datetime = datetime.minusHours(480);
            String afterSubtraction = datetime.format(formatter);
            Timestamp timestampAfterSubtraction = Timestamp.valueOf(afterSubtraction);
            if (user_i.getLastPositiveSwabDate() != null
                    && (user_i.getLastPositiveSwabDate().after(timestampAfterSubtraction)
                    || c.getStartDate().after(user_i.getLastPositiveSwabDate()))) {
                int prevValue = durationMap.get(user_j) == null ? 0 : durationMap.get(user_j);
                durationMap.put(user_j, (Integer) prevValue + c.getDuration());
            }
            if (user_j.getLastPositiveSwabDate() != null
                    && (user_j.getLastPositiveSwabDate().after(timestampAfterSubtraction)
                    || c.getStartDate().after(user_j.getLastPositiveSwabDate()))) {
                int prevValue = durationMap.get(user_i) == null ? 0 : durationMap.get(user_i);
                durationMap.put(user_i, (Integer) prevValue + c.getDuration());
            }

        }
        //end algorithm
        for (Map.Entry<User, Integer> entry : durationMap.entrySet()) {
            int value = entry.getValue();
            User u = entry.getKey();
            if (value > Config.RISK_MINUTES) {
                NotificationToken token = null;
                try {
                    Timestamp expireDate = Timestamp.valueOf(ServerUtils.getNow().toLocalDateTime().plusDays(Config.EXPIRE_DAYS));
                    token = new NotificationToken(u.getId(), expireDate, ServerUtils.toString(u.getCf()),
                            this.getSalt1(), this.getSalt2());
                    String code = token.getCode();
                    this.database.addNotification(code, u.getId());
                    this.database.updateUser(u.getCf(), null, ServerUtils.getNow(), null);
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;

    }

    public String getSalt1() {
        return salt1;
    }

    public String getSalt2() {
        return salt2;
    }
}
