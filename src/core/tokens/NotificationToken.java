/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import src.AppServer.ServerUtils;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import java.sql.Timestamp;

/**
 * Notification: BASE64(id, data_scadenza).HMACSHA256(BASE64(id, data_scadenza),SHA256(sale_2 || SHA256(sale_1  || codice_fiscale)))
 * */

public class NotificationToken extends BaseToken {
    private int id;
    private String cf;
    private Timestamp expireDate;

    public NotificationToken(int id, String cf, Timestamp expireData, String salt1, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        super(id+","+expireData.toString(),
                (MessageDigest.getInstance("SHA-256")).digest(
                        ServerUtils.concatByteArray(ServerUtils.toByteArray(salt2),
                            (MessageDigest.getInstance("SHA-256")).digest(
                                    ServerUtils.concatByteArray(
                                            ServerUtils.toByteArray(salt1), ServerUtils.toByteArray(cf))))));
        this.id = id;
        this.cf = cf;
        this.expireDate = expireData;
    }

    public int getId() {
        return id;
    }
    public String getCf() {
        return cf;
    }
    public Date getExpireDate() {
        return expireDate;
    }

    public String getToken() throws NoSuchAlgorithmException, InvalidKeyException {
        return this.getPayload() + "." + this.getSigma();
    }

    public boolean isValid(int id, String cf, Timestamp expireDate, String salt1, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {

        NotificationToken tmpNotificationToken = new NotificationToken(id, cf, expireDate, salt1, salt2);
        String tmpTokenCode = tmpNotificationToken.getToken();

        return this.getToken().equals(tmpTokenCode);
    }

}
