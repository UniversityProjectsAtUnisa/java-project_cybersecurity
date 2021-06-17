/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import exceptions.InvalidTokenFormat;
import src.AppServer.ServerUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

/**
 * Auth: BASE64(id, data_creazione).HMACSHA256(BASE64(id, data_creazione),
 * sale_2)
 *
 */
public final class AuthToken extends Token {

    public int getId() {
        String[] parts = this.getPayload().split(",");
        if (parts.length != 2) {
            throw new InvalidTokenFormat("The payload does not contain exactly one comma");
        }
        return Integer.parseInt(parts[0]);
    }

    public Timestamp getCreatedAt() {
        String[] parts = this.getPayload().split(",");
        if (parts.length != 2) {
            throw new InvalidTokenFormat("The payload does not contain exactly one comma");
        }
        return Timestamp.valueOf(parts[1]);
    }

    public AuthToken(int id, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        super(id + "," + ServerUtils.getNow(), ServerUtils.toByteArray(salt2));
    }

    public AuthToken(String raw) throws NoSuchAlgorithmException, InvalidKeyException {
        super(raw);
        checkPayloadFormat();
    }
    
    private void checkPayloadFormat() {
        getId();
        getCreatedAt();
    }

    @Override
    public String toString() {
        return "AuthToken: " + super.toString();
    }

    public boolean isValid(Timestamp lastLogin, String salt) throws NoSuchAlgorithmException, InvalidKeyException {
        Timestamp createdAt = this.getCreatedAt();
        if (!createdAt.equals(lastLogin)) {
            return false;
        }
        checkPayloadFormat();
        return this.verifySigma(ServerUtils.toByteArray(salt));
    }

}
