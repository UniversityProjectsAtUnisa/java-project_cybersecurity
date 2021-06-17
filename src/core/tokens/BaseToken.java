/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import src.AppServer.ServerUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

/**
 * Auth:         BASE64(id, data_creazione).HMACSHA256(BASE64(id, data_creazione), sale_2)
 * Notification: BASE64(id, data_scadenza).HMACSHA256(BASE64(id, data_scadenza),SHA256(sale_2 || SHA256(sale_1  || codice_fiscale)))
 */
public class BaseToken implements Serializable{
    private String payload;
    private String sigma;

    public BaseToken(String payload, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        this.payload = payload;
        byte[] bytePayload = ServerUtils.toByteArray(payload);

        Mac hMac = Mac.getInstance("HMacSHA256");
        Key hMacKey = new SecretKeySpec(key, "HMacSHA256");
        hMac.init(hMacKey);
        hMac.update(bytePayload);
        byte[] hMacRes = hMac.doFinal();

        this.sigma = ServerUtils.toString(hMacRes);
    }

    public BaseToken(String raw) {
        int separatorIndex = raw.indexOf(".");
        this.payload = raw.substring(0, separatorIndex);
        this.sigma = raw.substring(separatorIndex + 1);
    }

    public String getPayload() {
        return payload;
    }

    public String getSigma() {
        return sigma;
    }

}
