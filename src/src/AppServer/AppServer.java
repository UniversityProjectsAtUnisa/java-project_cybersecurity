/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppServer;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.sql.Timestamp;

import com.sun.security.ntlm.Server;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import core.tokens.*;
import entities.Contact;
import entities.User;
import entities.NotificationToken;

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

    private String salt1 = ""; //env.getSalt()
    private String salt2 = "";
    private Database database;

    public AppServer() {

        this.database = new Database();

        try {
            System.out.printf("Start App Server and init salts");
            String fileContent = ServerUtils.fileRead(".env");
            System.out.printf("Fetch salt from ev");
            int comaIndex = fileContent.indexOf(",");
            System.out.printf("Saving salts");
            this.salt1 = fileContent.substring(0, comaIndex);
            this.salt2 = fileContent.substring(comaIndex + 1, fileContent.length());
        } catch (IOException e){
            System.out.printf("Generate salt");
            byte[] byteSalt1 = new byte[32];
            byte[] byteSalt2 = new byte[32];
            new SecureRandom().nextBytes(byteSalt1);
            new SecureRandom().nextBytes(byteSalt2);
            System.out.printf("Saving salts generated");
            this.salt1 = ServerUtils.toString(byteSalt1);
            this.salt2 = ServerUtils.toString(byteSalt2);
            try {
                ServerUtils.fileWrite(".env", this.getSalt1() + "," + this.getSalt2());
            } catch(IOException ex){
                System.out.printf("Error while saving new salts");
            }
        }
    }

    public String login(String cf, String password) throws NoSuchAlgorithmException {
        User user = this.database.find_user(ServerUtils.toByteArray(cf));
        if (user == null){
            return "error"; //maybe an exception
        }
        byte[] userSalt = user.getSale_utente();
        byte[] passwordBytes = password.getBytes();

        byte[] passwordConcat = ServerUtils.concatByteArray(userSalt, passwordBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHased = md.digest(passwordConcat);

        if (passwordHased == user.getPassword()){
            Timestamp now = ServerUtils.getNow();
            int id = user.getId();
            this.database.update_user(user.getCf(), now, null, null);
            AuthToken token = new AuthToken(id);
            return token.getToken(this.getSalt2());
        }
        return false;
    }

    public boolean register(String cf, String password) throws NoSuchAlgorithmException {
        /*if (!HealhApi.checkCf(cf)){
            return false;
        }*/
        byte[] passwordBytes = password.getBytes();
        byte[] userSalt = new byte[32];
        new SecureRandom().nextBytes(userSalt);
        byte[] passwordConcat = ServerUtils.concatByteArray(passwordBytes, userSalt);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHased = md.digest(passwordConcat);

        //this.database.add_user(cf, passwordHased, userSalt);

        return true;
    }

    public boolean createReport(int id, int duration, Timestamp date, AuthToken token){
        String payload = token.getPayload();
        String strIdUser = payload.substring(0, payload.indexOf(","));
        int idUser = Integer.parseInt(strIdUser);
        User user = this.database.find_user(idUser);
        LinkedList<Contact> contacts = this.database.search_contacts_of_user(user.getCf());

        //algorithm
        for (Contact c: contacts) {
            if(c.getData_inizio_contatto().before(date)){
                //the contact in the table is longer than new contact advisor
              /*  if (data + duration < c.data + c.duration){
                    Database.Contact.create(user.id, user, duration, date);
                } else {
                    Database.Contact.create(user.id, user, c.duration, date);
                    Database.TempContact.delete(c);
                    Database.TempContact.insert(user.id, id, duration, date);
                }*/
            }
        }
        //end algorithm

        return true;
    }

    public NotificationToken[] getNotifications(core.tokens.NotificationToken token){
        String payload = token.getPayload();
        String strIdUser = payload.substring(0, payload.indexOf(","));
        int idUser = Integer.parseInt(strIdUser);
        User user = this.database.find_user(idUser);

        //return this.database.getNotifications(user.id);
    }

    public boolean useNotification(String code){
        NotificationToken notification = this.database.search_notificationToken(code);
        if (notification == null){
            return false;
        }
        NotificationToken token = this.database.update_notificationToken(code, ServerUtils.getNow());
        return true;
    }

    public NotificationToken getNotificationDetails(String code){
        return this.database.search_notificationToken(code);
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
                core.tokens.NotificationToken notificationT = new core.tokens.NotificationToken(u.getId(), ServerUtils.toString(u.getCf()),
                        ServerUtils.getNow());
                String code = null;
                try {
                    code = notificationT.getToken(this.getSalt1(), this.getSalt2());
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
