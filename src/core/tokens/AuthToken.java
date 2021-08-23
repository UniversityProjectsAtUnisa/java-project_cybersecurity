/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import exceptions.InvalidTokenFormatException;
import src.AppServer.ServerRunner;
import src.AppServer.ServerUtils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Auth: BASE64(id, data_creazione).HMACSHA256(BASE64(id, data_creazione),
 * sale_2)
 */
public final class AuthToken extends Token {

    private Cipher cipher = Cipher.getInstance("AES/CRC/PKCS5Padding");

    public byte[] getCfToken(SecretKey keyToken) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String[] parts = this.getPayload().split(",");
        if (parts.length != 2) {
            throw new InvalidTokenFormatException("The payload does not contain exactly one comma");
        }
        cipher.init(Cipher.DECRYPT_MODE, keyToken);
        byte[] decodedPayload =  cipher.doFinal(ServerUtils.toByteArray(parts[0]));
        String[] payloadParts = decodedPayload.toString().split("_");
        return ServerUtils.toByteArray(payloadParts[0]);
    }

    public Timestamp getCreatedAt(SecretKey keyToken) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        String[] parts = this.getPayload().split(",");
        if (parts.length != 2) {
            throw new InvalidTokenFormatException("The payload does not contain exactly one comma");
        }
        cipher.init(Cipher.DECRYPT_MODE, keyToken);
        byte[] decodedPayload =  cipher.doFinal(ServerUtils.toByteArray(parts[0]));
        String[] payloadParts = decodedPayload.toString().split("_");
        return Timestamp.valueOf(payloadParts[1]);
    }

    public AuthToken(byte[] hashedCf, SecretKey keyToken, byte[] saltToken, Timestamp creationDate) throws NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
        super(AuthToken.getEncryptedToken(hashedCf, keyToken, creationDate).toString(), saltToken);
    }

    static byte[] getEncryptedToken(byte[] hashedCf, SecretKey keyToken, Timestamp creationDate) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        String payloadStr = hashedCf.toString() + "_" + creationDate;
        cipher.init(Cipher.ENCRYPT_MODE, keyToken);
        return cipher.doFinal(ServerUtils.toByteArray(payloadStr));
    }

    public AuthToken(String raw, SecretKey keyToken) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        super(raw);
        checkPayloadFormat(keyToken);
    }

    private void checkPayloadFormat(SecretKey keyToken) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        getCfToken(keyToken);
        getCreatedAt(keyToken);
    }

    @Override
    public String toString() {
        return "AuthToken: " + super.toString();
    }

    public boolean isValid(Timestamp lastLogin, SecretKey keyToken, String saltToken) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Timestamp createdAt = this.getCreatedAt(keyToken);
        if (!createdAt.equals(lastLogin)) {
            return false;
        }
        checkPayloadFormat(keyToken);
        return this.verifySigma(ServerUtils.toByteArray(saltToken));
    }

}
