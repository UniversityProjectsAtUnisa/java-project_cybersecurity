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
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Auth:         BASE64(id, data_creazione).HMACSHA256(BASE64(id, data_creazione), sale_2)
 * */
public class AuthToken extends BaseToken  {

    private int id;

    public int getId() {
        return id;
    }

    public AuthToken(int id) {
        this.id = id;
        this.setPayload(id + "," + new Date().toString());
        this.setSigma(id + "," + new Date().toString()); //+ "," + env.getSalt2()
        this.setCreatedAt(ServerUtils.getNow());
    }

    public String getToken(String salt2) throws NoSuchAlgorithmException, InvalidKeyException {

        String payload = id + "," + new Date().toString();
        this.setPayload(payload);
        byte[] bytePayload = ServerUtils.toByteArray(this.getPayload());
        String hexPayload = ServerUtils.toHex(bytePayload);


        Mac hMac = Mac.getInstance("HMacSHA256");
        Key hMacKey = new SecretKeySpec(ServerUtils.toByteArray(salt2), "HMacSHA256");
        hMac.init(hMacKey);
        hMac.update(bytePayload);
        byte[] hMacRes = hMac.doFinal();
        this.setSigma(ServerUtils.toString(hMacRes));
        return this.getPayload() + "." + this.getSigma();
    }

    public boolean isValid(String salt2) throws NoSuchAlgorithmException, InvalidKeyException {
        String sigma = this.getSigma();
        byte[] valSigma = this.calcSigma(ServerUtils.toByteArray(salt2));
        String strValSigma = ServerUtils.toString(valSigma);

        return sigma == strValSigma;
    }

    private byte[] calcSigma(byte[] salt2) throws NoSuchAlgorithmException, InvalidKeyException {

        Mac hMac = Mac.getInstance("HMacSHA256");
        Key hMacKey = new SecretKeySpec(salt2, "HMacSHA256");
        hMac.init(hMacKey);
        hMac.update(ServerUtils.toByteArray(this.getPayload()));
        byte[] hMacRes = hMac.doFinal();
        return hMacRes;
    }
}
