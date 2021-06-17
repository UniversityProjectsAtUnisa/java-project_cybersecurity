/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import src.AppServer.ServerUtils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Auth:         BASE64(id, data_creazione).HMACSHA256(BASE64(id, data_creazione), sale_2)
 * */
public class AuthToken extends BaseToken  {

    private int id;
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public AuthToken(int id, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        super(id+","+ServerUtils.getNow(), ServerUtils.toByteArray(salt2));
        this.id = id;
        this.createdAt = ServerUtils.getNow();
    }

    public AuthToken(int id, Timestamp createdAt, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        super(id+","+createdAt, ServerUtils.toByteArray(salt2));
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getToken() {
        return this.getPayload()+"."+this.getSigma();
    }

    public boolean isValid(int id, Timestamp createdAt, String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        AuthToken tmpAuthToken = new AuthToken(id, createdAt, salt2);
        String tmpTokenCode = tmpAuthToken.getToken();
        return this.getToken().equals(tmpTokenCode);
    }

}
