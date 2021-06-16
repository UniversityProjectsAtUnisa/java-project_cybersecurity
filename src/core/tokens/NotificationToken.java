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

/**
 * Notification: BASE64(id, data_scadenza).HMACSHA256(BASE64(id, data_scadenza),SHA256(sale_2 || SHA256(sale_1  || codice_fiscale)))
 * */
public class NotificationToken extends BaseToken {
    private int id;

    public int getId() {
        return id;
    }

    public NotificationToken(int id, Date expireData) {
        this.setPayload(id + "," + expireData);
        this.setSigma(id + "," + expireData) ; //+ "," + env.getSalt2()
    }

    public String getToken(String cf, int id, String salt1, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        String expireDate = "";
        String payload = id + "," + expireDate;
        this.setPayload(payload);
        String hexPayload = ServerUtils.toHex(ServerUtils.toByteArray(this.getPayload()));
        byte[] bytePayload = ServerUtils.toByteArray(this.getPayload());

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] partialInSigma = md.digest(ServerUtils.toByteArray(salt1 + cf));

        byte[] secondPartSigma = md.digest(ServerUtils.concatByteArray(
                ServerUtils.toByteArray(salt2), partialInSigma));

        Mac hMac = Mac.getInstance("HMacSHA256");

        Key hMacKey = new SecretKeySpec(secondPartSigma, "HMacSHA256");
        hMac.init(hMacKey);
        hMac.update(bytePayload);

        byte[] hMacRes = hMac.doFinal();
        this.setSigma(ServerUtils.toString(hMacRes));

        return this.getPayload() + "." + this.getSigma();
    }
}
