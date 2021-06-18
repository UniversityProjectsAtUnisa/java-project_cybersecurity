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
import exceptions.DeletionFailedException;
import exceptions.InsertFailedException;
import exceptions.NotFoundException;
import exceptions.UpdateException;
import java.io.Serializable;
import java.util.logging.Level;
import utils.Config;

import javax.crypto.SecretKey;
import javax.naming.AuthenticationException;
import javax.xml.crypto.Data;
import utils.ContactReportMessage;
import utils.Credentials;
import java.text.MessageFormat;
import java.util.stream.Collectors;

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
public class AppServer {

    private String salt1 = "";
    private String salt2 = "";
    private final Database database;
    private final HAApiService healthApiService;
    private final SSLServer publicServer, restrictedServer;

    public AppServer(String password) throws IOException {
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
        SecretKey key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "salt1");
        this.salt1 = ServerUtils.toString(key1.getEncoded());
        SecretKey key2 = ServerUtils.loadFromKeyStore("./salts_keystore.jks", "changeit", "salt2");
        this.salt2 = ServerUtils.toString(key2.getEncoded());
    }

    public void start() {
        new Thread(restrictedServer::start).start();
        publicServer.start();
    }

    public synchronized Response restrictedHandleRequest(Request req) {
        try {
            String cf = (String) req.getPayload();
            boolean success = notifyPositiveUser(cf);
            return Response.make(success);
        } catch(Exception e) {
            Logger.getGlobal().warning("Server Internal Error: " + e.getMessage());
            return Response.error("Server Internal Error");
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
                Timestamp lastLoginDate = this.database.findUser(token.getId()).getLastLoginDate();
                if (!token.isValid(lastLoginDate, this.getSalt2())) {
                    throw new AuthenticationException("The authentication token is not valid");
                }
                loggedUser = this.database.findUser(token.getId());
                if (loggedUser == null) {
                    throw new AuthenticationException("User not found");
                }
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
                    data = this.createReport(createReportData.getIdUserToReport(), createReportData.getDuration(), createReportData.getStartDate(), loggedUser);
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

    public AuthToken login(String cf, String password) throws NoSuchAlgorithmException, InvalidKeyException, AuthenticationException, UpdateException {
        byte[] cfBytes = ServerUtils.toByteArray(cf);
        byte[] hashedCf = encryptCf(cfBytes);
        User user = this.database.findUser(hashedCf);
        if (user == null) {
            throw new AuthenticationException("Invalid credentials");
        }
        byte[] userSalt = user.getUserSalt();
        byte[] passwordBytes = ServerUtils.toByteArray(password);
        byte[] hashedPassword = encryptPassword(passwordBytes, userSalt);

        if (Arrays.equals(hashedPassword, user.getHashedPassword())) {
            int id = user.getId();
            AuthToken token = new AuthToken(id, this.getSalt2());
            if (this.database.updateUser(user.getHashedCf(), token.getCreatedAt(), null, null) == null) {
                throw new UpdateException("User update failed");
            }
            return token;
        }
        return null;
    }

    public boolean register(String cf, String password) throws NoSuchAlgorithmException, InsertFailedException {
        if (!healthApiService.checkCf(cf)){
            return false;
        }
        byte[] userSalt = new byte[32];
        new SecureRandom().nextBytes(userSalt);

        byte[] passwordBytes = password.getBytes();
        byte[] hashedPassword = encryptPassword(passwordBytes, userSalt);

        byte[] cfBytes = ServerUtils.toByteArray(cf);
        byte[] hashedCf = encryptCf(cfBytes);
        if (!this.database.addUser(hashedCf, hashedPassword, userSalt)) {
            throw new InsertFailedException("User registration failed");
        }

        return true;
    }

    private byte[] encryptPassword(byte[] plainPassword, byte[] userSalt) throws NoSuchAlgorithmException {
        byte[] combinedSalt = ServerUtils.concatByteArray(userSalt, ServerUtils.toByteArray(salt1));
        return ServerUtils.encryptWithSalt(plainPassword, combinedSalt);
    }

    private byte[] encryptCf(byte[] plainCf) throws NoSuchAlgorithmException {
        return ServerUtils.encryptWithSalt(plainCf, ServerUtils.toByteArray(salt1));
    }

    public boolean createReport(int id, int duration, Timestamp date, User loggedUser) throws NoSuchAlgorithmException, InvalidKeyException, NotFoundException, InsertFailedException, DeletionFailedException {
        System.out.println("CONTATTI NEL DB: " + this.database.contacts.size());
        System.out.println("REPORTS NEL DB: " + this.database.contactReports.size());
        byte[] cfReporter = loggedUser.getHashedCf();
        User reportedUser = this.database.findUser(id);
        if (reportedUser == null) {
            throw new NotFoundException("Reported user not found");
        }
        byte[] cfReported = reportedUser.getHashedCf();

        // FASE 1: Ricerca report nel database
        // FASE 2: Individuare sovrapposizioni
        // FASE 2.1: Creare i contatti per le sovrapposizioni
        // FASE 3: Creare il nuovo report se è il più recente
        // FASE 4: Cancellare i vecchi report
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
        for (ContactReport overlap : overlaps) {
            if (!this.database.addContact(overlap)) {
                throw new InsertFailedException("Contact creation failed");
            }
        }

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
            if (!this.database.addContactReport(newReport)) {
                throw new InsertFailedException("Contact report creation failed");
            }
        }

        // FASE 4
        // Cancello tutti i report tranne il più recente
        for (ContactReport r : reports) {
            if (!r.equals(mostRecentReport)) {
                if(!this.database.removeContactReport(r.getReporterId(), r.getReportedId(), r.getStartDate())) {
                    throw new DeletionFailedException("Impossible to remove contact");
                }
            }
        }

        return true;
    }

    public LinkedList<String> getNotifications(User loggedUser) throws NoSuchAlgorithmException, InvalidKeyException {
        return this.database
                .searchUserNotifications(loggedUser.getId())
                .stream()
                .map(notification -> notification.getCode())
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public boolean useNotification(String code) throws NotFoundException, UpdateException {
        // TODO: MANCA
        Notification notification = this.database.searchNotification(code);
        if (notification == null) {
            throw new NotFoundException("Notification with given code not found");
        }
        if(this.database.updateNotification(code, ServerUtils.getNow()) == null){
            throw new UpdateException("Impossible to update notification");
        }
        return true;
    }

    public Timestamp getNotificationSuspensionDate(String code, User loggedUser) throws AuthenticationException, NotFoundException {
        Notification notification = this.database.searchNotification(code);
        if (notification == null) {
            throw new NotFoundException("Notification with given code not found");
        }
        if (notification.getId() != loggedUser.getId()) {
            throw new AuthenticationException("The logged user does not own the selected notificationToken");
        }
        return notification.getSuspensionDate();
    }

    public boolean notifyPositiveUser(String cf) throws NoSuchAlgorithmException, InvalidKeyException, NotFoundException, UpdateException {
        // TODO: MANCA
        byte[] cfBytes = ServerUtils.toByteArray(cf);
        byte[] hashedCf = encryptCf(cfBytes);
        User user = this.database.findUser(hashedCf);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        List<Contact> contacts = this.database
                .searchContactsOfUser(hashedCf)
                .stream()
                .filter(contact -> contact.getEndDate().after(ServerUtils.getLastValidDateForContact()))
                .toList();
        if (this.database.updateUser(hashedCf, null, null, ServerUtils.getNow()) == null) {
            throw new UpdateException("Impossibile to update user");
        }

        // algorithm
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
        // end algorithm
        durationMap.entrySet().forEach(entry -> {
            int value = entry.getValue();
            User u = entry.getKey();
            if (value > Config.RISK_MINUTES) {
                NotificationToken token = null;
                try {
                    Timestamp expireDate = Timestamp.valueOf(ServerUtils.getNow().toLocalDateTime().plusDays(Config.EXPIRE_DAYS));
                    token = new NotificationToken(u.getId(), expireDate, ServerUtils.toString(u.getHashedCf()),
                            this.getSalt1(), this.getSalt2());
                    String code = token.getCode();
                    this.database.addNotification(code);
                    this.database.updateUser(u.getHashedCf(), null, ServerUtils.getNow(), null);
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        });

        return true;

    }

    private String getSalt1() {
        return salt1;
    }

    private String getSalt2() {
        return salt2;
    }
}
