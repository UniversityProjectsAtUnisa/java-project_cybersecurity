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

import com.sun.security.ntlm.Server;
import core.tokens.*;

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

    public AppServer() {
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
/*
    public String login(String cf, String password) throws NoSuchAlgorithmException {
        User user = Database.User.getByCf(cf);
        if (User == false){
            return "error"; //maybe an exception
        }
        String userSalt = User.getSalt();
        byte[] byteUserSalt = ServerUtils.toByteArray(userSalt);
        byte[] passwordBytes = password.getBytes();

        byte[] passwordConcat = ServerUtils.concatByteArray(byteUserSalt, passwordBytes);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHased = md.digest(passwordConcat);

        if (passwordHased == user.password){
            Database.User.update(id, cf, Date.now());
            AuthToken token = new AuthToken();
            Database.updateToken(use.id, token);
            return token.getToken(id, this.getSalt2());
        }
    }

    public Boolean register(String cf, String password) throws NoSuchAlgorithmException {
        if (!HealhApi.checkCf(cf)){
            return false;
        }
        byte[] passwordBytes = password.getBytes();
        byte[] userSalt = new byte[32];
        new SecureRandom().nextBytes(userSalt);
        byte[] passwordConcat = new byte[passwordBytes.length + userSalt.length];
        int pos = 0;
        for (byte element : passwordBytes) {
            passwordConcat[pos] = element;
            pos++;
        }

        for (byte element : userSalt) {
            passwordConcat[pos] = element;
            pos++;
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] passwordHased = md.digest(passwordConcat);

        Database.User.create(cf, passwordHased, userSalt);
        return true;
    }

    public Boolean createReport(int id, int duration, Date date, String token){
        User user = Database.User.getUserByToken(token);
        Contact[] contacts = Database.TempContacts.getById(user.id);

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

    public Notification[] getNotifications(String token){
        User user = Database.User.getByToken(token);
        Notification[] notifications = Database.Notification.get(user.id);
        return notification;
    }

    public Boolean useNotification(String code){
        Notification notification = Database.Notification.get(code);
        if (tampon == null){
            return false;
        }
        notification.setSuspension(new Date().now);
        Database.Notification.update(notification);
        return true;
    }

    public Notification getNotificationDetails(String code){
        Notification notification = Database.Notification.get(code);
        return notification;
    }

    public Boolean notifyPositiveUser(String cf){
        User user = Database.User.getByCf(cf);
        Contact[] contacts = Database.Contact.get(user.id);

        //algorithm
        for (Contact c: contacts) {
            Map durationMap = new HashMap<String, Integer>();
            User user_i = Database.User.getById(id1);
            User user_j = Database.User.getById(id2);
               if (user_i.data_ultimo_tampone < Date.now() - 20 days &&  c.date > user_j.data_ultimo_tampone) {

                    //durationMap.
                    //durationMap.add(user_i)
                int x = 0;
                } else if( user_i.data_ultimo_tampone < Date.now() - 20 days and c.date > user_j.data_ultimo_tampone) {
                    //tempo[user_i] += minuti
                }

        }


        //end algorithm

        for (User u: riskUser) {
            String token = new NotificationToken().getToken(u.id, u.cf, this.getSalt1(), this.getSalt2());
            Database.Notification.create(u.id, u.cf, token);
        }

    }*/

    public String getSalt1() {
        return salt1;
    }

    public String getSalt2() {
        return salt2;
    }
}
