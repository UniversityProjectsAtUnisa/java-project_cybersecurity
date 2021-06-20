/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import exceptions.InvalidTokenFormatException;
import src.AppServer.ServerUtils;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import java.sql.Timestamp;
import src.AppServer.Database;

/**
 * Notification: BASE64(id, data_scadenza).HMACSHA256(BASE64(id,
 * data_scadenza),SHA256(sale_2 || SHA256(sale_1 || codice_fiscale)))
 *
 */
public final class NotificationToken extends Token {

    public int getId() {
        String[] parts = this.getPayload().split(",");
        if (parts.length != 2) {
            throw new InvalidTokenFormatException("The payload does not contain exactly one comma");
        }
        return Integer.parseInt(parts[0]);
    }

    public Timestamp getExpireDate() {
        String[] parts = this.getPayload().split(",");
        if (parts.length != 2) {
            throw new InvalidTokenFormatException("The payload does not contain exactly one comma");
        }
        return Timestamp.valueOf(parts[1]);
    }

    public NotificationToken(int id, Timestamp expireDate, String cf, String salt1, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        super(id + "," + expireDate.toString(), getNotificationTokenKey(cf, salt1, salt2));
    }
    
    public NotificationToken(int id, Timestamp expireDate, byte[] hashedCf, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        super(id + "," + expireDate.toString(), getNotificationTokenKey(hashedCf, salt2));
    }

    public NotificationToken(String raw) throws NoSuchAlgorithmException, InvalidKeyException {
        super(raw);
        checkPayloadFormat();
    }

    private void checkPayloadFormat() {
        getId();
        getExpireDate();
    }

    private static byte[] getNotificationTokenKey(String cf, String salt1, String salt2) throws NoSuchAlgorithmException {
        byte[] cfBytes = ServerUtils.toByteArray(cf);
        byte[] salt1Bytes = ServerUtils.toByteArray(salt1);
        byte[] salt2Bytes = ServerUtils.toByteArray(salt2);
        byte[] hashedCf = ServerUtils.encryptWithSalt(cfBytes, salt1Bytes);
        return ServerUtils.encryptWithSalt(hashedCf, salt2Bytes);
    }
    
    private static byte[] getNotificationTokenKey(byte[] hashedCf, String salt2) throws NoSuchAlgorithmException {
        byte[] salt2Bytes = ServerUtils.toByteArray(salt2);
        return ServerUtils.encryptWithSalt(hashedCf, salt2Bytes);
    }

    @Override
    public String toString() {
        return "NotificationToken: " + super.toString();
    }

    public boolean isValid(String salt) throws NoSuchAlgorithmException, InvalidKeyException {
        if (getExpireDate().before(ServerUtils.getNow())) {
            return false;
        }
        checkPayloadFormat();
        return this.verifySigma(ServerUtils.toByteArray(salt));
    }

    public boolean isValid(String cf, String salt1, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        if(!this.isValid(salt2)){
            return false;
        }
        
        NotificationToken token = new NotificationToken(this.getId(), this.getExpireDate(), cf, salt1, salt2);
        return token.equals(token);
    }
}
