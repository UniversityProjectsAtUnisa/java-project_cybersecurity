/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppServer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.*;
import java.sql.Timestamp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import core.tokens.*;
import entities.Contact;
import entities.ContactReport;
import entities.User;

import javax.crypto.SecretKey;
import javax.xml.crypto.Data;

/**
 *
 * Endpoints:
 * Login
 *      login({codice_fiscale, password}) -> token
 * Registrazione
 *      register({codice_fiscale, password}) -> boolean
 * Segnalazione contatto
 *      createReport({id2, duration, data}, token) -> boolean
 * Richiesta notifiche
 *      getNotifications(token) -> Notification[]
 * Verifica e Disabilita codice tampone gratuito
 *      useNotification({code}) -> boolean
 * Richiesta informazioni codice tampone gratuito
 *      getNotificationDetails({code}, token) -> NotificationDetail
 * Tampone con risultato positivo dall’HA
 *      notifyPositiveUser({codice_fiscale}) -> boolean
 *
 */

public class AppServer {

    private String salt1 = "";
    private String salt2 = "";
    private Database database;

    public AppServer() {
        this.database = new Database();
        SecretKey key1 = ServerUtils.loadFromKeyStore("./salts_keystore.jks","changeit","salt1");
        this.salt1 = ServerUtils.toString(key1.getEncoded());
        SecretKey key2 = ServerUtils.loadFromKeyStore("./salts_keystore.jks","changeit","salt2");
        this.salt2 = ServerUtils.toString(key2.getEncoded());
    }
/*
    public <T> void handleRequest(Request req){
        <V> payload = req.getPayload();
        <T> response;
        switch(req.getName()){
            case 'login':
                response = this.login(payload.getCf(), payload.getPassword());
                break;
            case 'register':
                response = this.register(payload.getCf(), payload.getPassword());
                break;
            case 'createReport':
                response = this.createReport(payload.getId(), payload.getDuration(), payload.getDate(), payload.getToken());
                break;
            case 'getNotifications':
                response = this.getNotifications(payload.getToken());
                break;
            case 'useNotifications':
                response = this.useNotification(payload.getCode());
                break;
            case 'getNotificationsDetails':
                response = this.getNotificationsDetails(payload.getCode());
                break;
            case 'notifyPositiveUser':
                response = this.notifyPositiveUser(payload.getCf());
                break;
        }
        return response;
    }
*/
    public AuthToken login(String cf, String password) throws NoSuchAlgorithmException, InvalidKeyException {
        User user = this.database.findUser(ServerUtils.toByteArray(cf));
        if (user == null){
            return null;
        }
        byte[] userSalt = user.getUserSalt();
        byte[] passwordBytes = password.getBytes();

        byte[] passwordConcat = ServerUtils.concatByteArray(passwordBytes, userSalt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHashed = md.digest(passwordConcat);
        if (Arrays.equals(passwordHashed, user.getPassword())){
            Timestamp now = ServerUtils.getNow();
            int id = user.getId();
            this.database.updateUser(user.getCf(), ServerUtils.getNow(), null, null);
            AuthToken token = new AuthToken(id, this.getSalt2());
            return token;
        }
        return null;
    }

    public boolean register(String cf, String password) throws NoSuchAlgorithmException {
        /* if (!HealhApi.checkCf(cf)){
            return false;
        }*/
        byte[] passwordBytes = password.getBytes();
        byte[] userSalt = new byte[32];
        new SecureRandom().nextBytes(userSalt);
        byte[] passwordConcat = ServerUtils.concatByteArray(passwordBytes, userSalt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHashed = md.digest(passwordConcat);

        this.database.addUser(ServerUtils.toByteArray(cf), passwordHashed, userSalt);

        return true;
    }

    public boolean createReport(int id, int duration, Timestamp date, AuthToken token) throws NoSuchAlgorithmException, InvalidKeyException {
        if(!token.isValid(token.getId(), token.getCreatedAt(), this.getSalt2())){
            return false;
        }
        System.out.println("crea il report");
        int idUser = token.getId();
        User user = this.database.findUser(idUser);
        byte[] cfReporter = user.getCf();
        byte[] cfReported = this.database.findUser(id).getCf();

        LinkedList<ContactReport> reports = this.database.searchContactReportOfReported(cfReporter);
        System.out.println("\n"+reports.size()+"\n");

        reports.forEach((c) -> {
            System.out.println("reportedId" + c.getReportedId());
        });

        if (reports.size() == 0){
            this.database.addContactReport(cfReporter, cfReported, duration, date);
            return true;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");
        Timestamp cEndDateMax =  new Timestamp(0);

        LocalDateTime reportEndDate = date.toLocalDateTime().plusSeconds(duration);
        String strReportEndDate = reportEndDate.format(formatter);
        Timestamp timestampReportEndDate = Timestamp.valueOf(strReportEndDate);
        //algorithm
        for (ContactReport c: reports) {
            LocalDateTime datetime = c.getStartContactDate().toLocalDateTime();

            datetime=datetime.minusMinutes(15);
            String afterSubtraction=datetime.format(formatter);
            Timestamp timestampAfterSubtraction = Timestamp.valueOf(afterSubtraction);

            LocalDateTime cEndDate = c.getStartContactDate().toLocalDateTime().plusSeconds(c.getDuration());
            String strCEndDate = cEndDate.format(formatter);
            Timestamp timestampCEndDate = Timestamp.valueOf(strCEndDate);

            cEndDateMax = ServerUtils.maxTimestamp(cEndDateMax, timestampCEndDate);

            if((!c.getStartContactDate().after(date) && !date.after(timestampCEndDate) ||
                    !c.getStartContactDate().before(date) && !timestampReportEndDate.before(c.getStartContactDate())) &&
                    !c.getStartContactDate().before(timestampAfterSubtraction)){

                System.out.println("la data viene prim senza aggiungere la durata");

                LocalDateTime cDate = c.getStartContactDate().toLocalDateTime().plusSeconds(c.getDuration());

                if (timestampReportEndDate.after(timestampCEndDate)){
                    this.database.removeContactReport(cfReported, cfReporter, c.getStartContactDate());
                }

                Timestamp maxInit = ServerUtils.maxTimestamp(date, c.getStartContactDate());
                Timestamp minEnd = ServerUtils.minTimestamp(timestampReportEndDate, timestampCEndDate);
                int durationNewContact = ServerUtils.diffTimestampSec(minEnd, maxInit);

                System.out.println("init: "+maxInit.toString()+ ", end:"+minEnd.toString()+", duration:" + durationNewContact);

                this.database.addContact(cfReporter, cfReported, durationNewContact, maxInit);

            }else{
                System.out.println("la data viene dopo senza aggiungere la durata");
            }
        }
        if (!timestampReportEndDate.before(cEndDateMax)) {
            this.database.addContactReport(cfReporter, cfReported, duration, date);
        }
        //end algorithm

        return true;
    }

    public LinkedList<String> getNotifications(core.tokens.NotificationToken token) throws NoSuchAlgorithmException, InvalidKeyException {

        String payload = token.getPayload();
        String strIdUser = payload.substring(0, payload.indexOf(","));
        int idUser = Integer.parseInt(strIdUser);
        User user = this.database.findUser(idUser);
        if(!token.isValid(idUser, ServerUtils.toString(user.getCf()), ServerUtils.getNow(), this.getSalt1(), this.getSalt2())){
            return null;
        }
        LinkedList<entities.NotificationToken> dbTokens = this.database.searchUserNotificationTokens(user.getId());
        LinkedList<String> tokens = new LinkedList<>();

        for (entities.NotificationToken dbToken: dbTokens) {
            tokens.add(dbToken.getCode());
        }

        return tokens;
    }

    public boolean useNotification(String code){
        entities.NotificationToken notification = this.database.searchNotificationToken(code);
        if (notification == null){
            return false;
        }
        entities.NotificationToken token = this.database.updateNotificationToken(code, ServerUtils.getNow());
        return true;
    }

    public entities.NotificationToken getNotificationDetails(String code){
        return this.database.searchNotificationToken(code);
    }

    public boolean notifyPositiveUser(String cf) throws NoSuchAlgorithmException, InvalidKeyException {
        User user = this.database.findUser(ServerUtils.toByteArray(cf));
        LinkedList<Contact> contacts = this.database.searchContactsOfUser(user.getCf());
        user.setLastPositiveSwabDate(ServerUtils.getNow());
        this.database.updateUser(user.getCf(), null, null, ServerUtils.getNow());

        //algorithm
        Map<User, Integer> durationMap = new HashMap<>();
        for (Contact c: contacts) {
            User user_i = this.database.findUser(c.getReporterId());
            User user_j = this.database.findUser(c.getReportedId());
            LocalDateTime datetime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");


            datetime=datetime.minusHours(480);
            String afterSubtraction=datetime.format(formatter);
            Timestamp timestampAfterSubtraction = Timestamp.valueOf(afterSubtraction);
            if (user_i.getLastPositiveSwabDate() != null &&
                    (user_i.getLastPositiveSwabDate().after(timestampAfterSubtraction)  ||
                    c.getStartContactDate().after(user_i.getLastPositiveSwabDate()))){
                int prevValue = durationMap.get(user_j) == null ? 0 : (Integer)durationMap.get(user_j);
                durationMap.put(user_j, (Integer)prevValue + c.getDuration());
            }
            if (user_j.getLastPositiveSwabDate() != null &&
                    (user_j.getLastPositiveSwabDate().after(timestampAfterSubtraction)  ||
                    c.getStartContactDate().after(user_j.getLastPositiveSwabDate()))){
                int prevValue = durationMap.get(user_i) == null ? 0 : (Integer)durationMap.get(user_i);
                durationMap.put(user_i, (Integer)prevValue + c.getDuration());
            }

        }
        //end algorithm
        for (Map.Entry<User,Integer> entry : durationMap.entrySet()){
            int value = entry.getValue();
            User u = entry.getKey();
            if ((Integer)value > (Integer)15){
                NotificationToken notificationT = null;
                try {
                    notificationT = new NotificationToken(u.getId(), ServerUtils.toString(u.getCf()),
                            ServerUtils.getNow(), this.getSalt1(), this.getSalt2());
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                String code = null;
                try {
                    code = notificationT.getToken();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
                this.database.addNotificationToken(code, u.getId());
                this.database.updateUser(u.getCf(), null, ServerUtils.getNow(), null);
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

    //develop
    public Database getDatabase() {return this.database;}
}
