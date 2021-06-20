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
import exceptions.InvalidTokenFormatException;
import exceptions.InvalidTokenSigmaException;
import java.util.Objects;

/**
 * Auth: BASE64(id, data_creazione).HMACSHA256(BASE64(id, data_creazione),
 * sale_2) Notification: BASE64(id, data_scadenza).HMACSHA256(BASE64(id,
 * data_scadenza),SHA256(sale_2 || SHA256(sale_1 || codice_fiscale)))
 */
public abstract class Token implements Serializable, Comparable<Token> {

    private String payload;
    private String sigma;

    public Token(String payload, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        this.payload = payload;
        this.sigma = calculateSigma(payload, key);
    }

    private String calculateSigma(String payload, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] bytePayload = ServerUtils.toByteArray(payload);
        Mac hMac = Mac.getInstance("HMacSHA256");
        Key hMacKey = new SecretKeySpec(key, "HMacSHA256");
        hMac.init(hMacKey);
        hMac.update(bytePayload);
        byte[] hMacRes = hMac.doFinal();
        return ServerUtils.toString(hMacRes);
    }

    public Token(String raw) {
        String[] parts = raw.split(".");
        if (parts.length != 2) {
            throw new InvalidTokenFormatException("The raw string does not contain only one .");
        }
        this.payload = parts[0];
        this.sigma = parts[1];
    }

    public String getPayload() {
        return payload;
    }

    public String getSigma() {
        return sigma;
    }

    public String getCode() {
        return this.getPayload() + "." + this.getSigma();
    }

    @Override
    public String toString() {
        return getCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Token other = (Token) obj;
        if (!Objects.equals(this.payload, other.payload)) {
            return false;
        }
        if (!ServerUtils.dumbStringCompare(this.sigma, other.sigma)) {
            return false;
        }
        return true;
    }

    protected boolean verifySigma(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        String calculatedSigma = this.calculateSigma(this.payload, key);
        return ServerUtils.dumbStringCompare(calculatedSigma, this.sigma);
    }

    @Override
    public int compareTo(Token o) {
        int c = payload.compareTo(o.getPayload());
        if (c == 0) {
            c = sigma.compareTo(o.getSigma());
        }
        return c;
    }
}
