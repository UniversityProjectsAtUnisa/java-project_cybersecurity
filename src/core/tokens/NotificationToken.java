/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import src.AppServer.ServerUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
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
    private Date expireDate;

    public NotificationToken(int id, String cf, Timestamp expireData) {
        this.id = id;
        this.cf = cf;
        this.expireDate = expireData;
        this.setPayload(id + "," + expireData);
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

    public String getToken(String salt1, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        String expireDate = "";
        String payload = id + "," + expireDate;
        this.setPayload(payload);

        byte[] byteSalt1 = ServerUtils.toByteArray(salt1);
        byte[] byteSalt2 = ServerUtils.toByteArray(salt2);

        byte[] hMacRes = this.calcSigma(byteSalt1, byteSalt2);

        this.setSigma(ServerUtils.toString(hMacRes));

        return this.getPayload() + "." + this.getSigma();
    }

    public boolean isValid(String salt1, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        String tmpSigma = this.getSigma();
        byte[] byteSalt1 = ServerUtils.toByteArray(salt1);
        byte[] byteSalt2 = ServerUtils.toByteArray(salt2);

        byte[] valSigma = this.calcSigma(byteSalt1, byteSalt2);
        String strValSigma = ServerUtils.toString(valSigma);

        return tmpSigma == strValSigma;
    }

    private byte[] calcSigma(byte[] salt1, byte[] salt2) throws NoSuchAlgorithmException, InvalidKeyException {

        byte[] bytePayload = ServerUtils.toByteArray(this.getPayload());

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] partialInSigma = md.digest(ServerUtils.concatByteArray(salt1, ServerUtils.toByteArray(this.getCf()))); //SHA256(sale_1  || codice_fiscale)

        byte[] hMacParam = md.digest(ServerUtils.concatByteArray(
                salt2, partialInSigma)); // SHA256(sale_2 || SHA256(sale_1  || codice_fiscale)

        byte[] clearInfo = ServerUtils.toByteArray(this.getId()+","+this.getExpireDate()); //BASE64(id, data_scadenza)

        Mac hMac = Mac.getInstance("HMacSHA256");

        Key hMacKey = new SecretKeySpec(hMacParam, "HMacSHA256");
        hMac.init(hMacKey);
        hMac.update(bytePayload);

        byte[] hMacRes = hMac.doFinal();
        return hMacRes;
    }

}
