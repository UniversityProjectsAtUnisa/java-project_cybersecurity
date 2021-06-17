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
    public String login(String cf, String password) throws NoSuchAlgorithmException, InvalidKeyException {
        User user = this.database.find_user(ServerUtils.toByteArray(cf));
        if (user == null){
            return "error"; //maybe an exception
        }
        byte[] userSalt = user.getSale_utente();
        byte[] passwordBytes = password.getBytes();

        byte[] passwordConcat = ServerUtils.concatByteArray(userSalt, passwordBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHashed = md.digest(passwordConcat);

        if (passwordHashed == user.getPassword()){
            Timestamp now = ServerUtils.getNow();
            int id = user.getId();
            this.database.update_user(user.getCf(), ServerUtils.getNow(), null, null);
            AuthToken token = new AuthToken(id, this.getSalt2());
            return token.getToken();
        }
        return "error";
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

        this.database.add_user(ServerUtils.toByteArray(cf), passwordHashed, userSalt);

        return true;
    }

    public boolean createReport(int id, int duration, Timestamp date, AuthToken token) throws NoSuchAlgorithmException, InvalidKeyException {
        if(!token.isValid(id, this.getSalt2())){
            return false;
        }

        String payload = token.getPayload();
        String strIdUser = payload.substring(0, payload.indexOf(","));
        int idUser = Integer.parseInt(strIdUser);
        User user = this.database.find_user(idUser);
        byte[] cfSegnalatore = user.getCf();
        byte[] cfSegnalato = this.database.find_user(id).getCf();


        LinkedList<ContactReport> contacts = this.database.search_contactReport_of_users(cfSegnalato, cfSegnalatore);

        //algorithm
        for (ContactReport c: contacts) {
            if(c.getData_inizio_contatto().before(date)){
                //the contact in the table is longer than new contact advisor
                LocalDateTime datetime = date.toLocalDateTime();
                LocalDateTime cDate = c.getData_inizio_contatto().toLocalDateTime().plusSeconds(c.getDurata());

                if (datetime.plusSeconds(duration).isBefore(cDate)){
                    this.database.add_contact(cfSegnalatore, cfSegnalato, duration, date);
                } else {
                    this.database.add_contact(cfSegnalatore, cfSegnalato, c.getDurata(), date);
                    this.database.remove_contactReport(cfSegnalato, cfSegnalatore, c.getData_inizio_contatto());
                    this.database.add_contactReport(cfSegnalatore, cfSegnalato, duration, date);
                }
            }
        }
        //end algorithm

        return true;
    }

    public LinkedList<String> getNotifications(core.tokens.NotificationToken token) throws NoSuchAlgorithmException, InvalidKeyException {

        String payload = token.getPayload();
        String strIdUser = payload.substring(0, payload.indexOf(","));
        int idUser = Integer.parseInt(strIdUser);
        User user = this.database.find_user(idUser);
        if(!token.isValid(idUser, ServerUtils.toString(user.getCf()), ServerUtils.getNow(), this.getSalt1(), this.getSalt2())){
            return null;
        }
        LinkedList<entities.NotificationToken> dbTokens = this.database.search_notificationTokens_user(user.getId());
        LinkedList<String> tokens = new LinkedList<>();

        for (entities.NotificationToken dbToken: dbTokens) {
            tokens.add(dbToken.getCodice());
        }

        return tokens;
    }

    public boolean useNotification(String code){
        entities.NotificationToken notification = this.database.search_notificationToken(code);
        if (notification == null){
            return false;
        }
        entities.NotificationToken token = this.database.update_notificationToken(code, ServerUtils.getNow());
        return true;
    }

    public String getNotificationDetails(String code){
        return this.database.search_notificationToken(code).getCodice();
    }

    public boolean notifyPositiveUser(String cf) throws NoSuchAlgorithmException, InvalidKeyException {
        User user = this.database.find_user(ServerUtils.toByteArray(cf));
        LinkedList<Contact> contacts = this.database.search_contacts_of_user(user.getCf());

        //algorithm
        HashMap durationMap = new HashMap<User, Integer>();
        for (Contact c: contacts) {
            User user_i = this.database.find_user(c.getId_segnalatore());
            User user_j = this.database.find_user(c.getId_segnalato());
            LocalDateTime datetime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");


            datetime=datetime.minusHours(480);
            String afterSubtraction=datetime.format(formatter);
            Timestamp timestampAfterSubtraction = Timestamp.valueOf(afterSubtraction);
            if (user_i.getData_ultimo_tampone_positivo().after(timestampAfterSubtraction)  &&
                    c.getData_inizio_contatto().after(user_i.getData_ultimo_tampone_positivo())){
                durationMap.put(user_i, (Integer)durationMap.get(user_i) + c.getDurata());
            }
            if (user_j.getData_ultimo_tampone_positivo().after(timestampAfterSubtraction)  &&
                    c.getData_inizio_contatto().after(user_j.getData_ultimo_tampone_positivo())){
                durationMap.put(user_j, (Integer)durationMap.get(user_j) + c.getDurata());
            }

        }
        //end algorithm

        durationMap.forEach ((key, value) ->{
            if ((Integer)value > (Integer)15){
                User u = (User)key;
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
                this.database.add_notificationToken(code, u.getId());
            }
        });

        return true;

    }

    public String getSalt1() {
        return salt1;
    }

    public String getSalt2() {
        return salt2;
    }
}
