/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppServer;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
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
        String userSalt = user.getSale_utente();
        byte[] byteUserSalt = ServerUtils.toByteArray(userSalt);
        byte[] passwordBytes = password.getBytes();

        byte[] passwordConcat = ServerUtils.concatByteArray(byteUserSalt, passwordBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHased = md.digest(passwordConcat);

        if (passwordHased == user.password){
            Timestamp now = ServerUtils.getNow();
            this.database.update_user(user.getCf(), now, null, null);
            AuthToken token = new AuthToken(user.id);
            Database.updateToken(use.id, token);
            return token.getToken(id, this.getSalt2());
        }
    }

    public boolean register(String cf, String password) throws NoSuchAlgorithmException {
        if (!HealhApi.checkCf(cf)){
            return false;
        }
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
        String strIdUser = payload.subString(0, payload.indexOf(","));
        int idUser = Integer.parseInt(strId);
        User user = this.database.find_user(idUser);
        //Contact[] contacts = Database.search_contactReport(user.id);

        //algorithm
        for (Contact c: contacts) {
            if(c.date <= date){
                //the contact in the table is longer than new contact advisor
                if (data + duration < c.data + c.duration){
                    Database.Contact.create(user.id, user, duration, date);
                } else {
                    Database.Contact.create(user.id, user, c.duration, date);
                    Database.TempContact.delete(c);
                    Database.TempContact.insert(user.id, id, duration, date);
                }
            }
        }
        //end algorithm

        return true;
    }

    public NotificationToken[] getNotifications(String token){
        String payload = token.getPayload();
        String strIdUser = payload.subString(0, payload.indexOf(","));
        int idUser = Integer.parseInt(strId);
        User user = this.database.getUser(idUser);

        return this.database.getNotifications(user.id);
    }

    public boolean useNotification(String code){
        NotificationToken notification = this.database.search_notificationToken(code);
        if (notification == null){
            return false;
        }
        return true;
    }

    public NotificationToken getNotificationDetails(String code){
        return this.database.search_notificationToken(code);
    }

    public boolean notifyPositiveUser(String cf){
        User user = Database.User.getByCf(cf);
        LinkedList<Contact> contacts = this.database.search_contacts_of_user(user.getCf());

        //algorithm
        for (Contact c: contacts) {
            Map durationMap = new HashMap<String, Integer>();
            User user_i = this.database.getUser(id1);
            User user_j = this.database.getUser(id2);
            LocalDateTime datetime = LocalDateTime.now();
            System.out.println("Before subtraction of hours from date: "+datetime.format(formatter));

            datetime=datetime.minusDays(20);
            String aftersubtraction=datetime.format(formatter);
            System.out.println("After 1 hour subtraction from date: "+aftersubtraction);

        }
        //end algorithm

        for (User u: riskUser) {
            Timestamp instant= Timestamp.from(Instant.now());
            //Per il momento metto come data di scadenza la data di adesso
            NotificationToken token = new NotificationToken(u.id, u.cf, instant);
            String strToken = token.getToken(this.getSalt1(), this.getSalt2());
            this.database.add_notificationToken(strToken);
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
