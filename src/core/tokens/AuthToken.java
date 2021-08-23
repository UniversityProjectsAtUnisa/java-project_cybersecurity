/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core.tokens;

import exceptions.InvalidTokenFormatException;
import src.AppServer.ServerRunner;
import src.AppServer.ServerUtils;
import utils.BytesUtils;

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

    public AuthToken(byte[] hashedCf, SecretKey keyToken, byte[] saltToken, Timestamp creationDate) throws NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
        super(BytesUtils.toString(AuthToken.getEncryptedToken(hashedCf, keyToken, creationDate)), saltToken);
    }

    public byte[] getCfToken(SecretKey keyToken) {
        String[] payloadParts = getDecryptedPayload(keyToken);
        return ServerUtils.toByteArray(payloadParts[0]);
    }

    public Timestamp getCreatedAt(SecretKey keyToken) {
        String[] payloadParts = getDecryptedPayload(keyToken);
        return Timestamp.valueOf(payloadParts[1]);
    }

    static byte[] getEncryptedToken(byte[] hashedCf, SecretKey keyToken, Timestamp creationDate) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        String payloadStr = BytesUtils.toString(hashedCf) + "_" + creationDate;
        cipher.init(Cipher.ENCRYPT_MODE, keyToken);
        return cipher.doFinal(ServerUtils.toByteArray(payloadStr));
    }

    @Override
    public String toString() {
        return "AuthToken: " + super.toString();
    }

    public boolean isValid(Timestamp lastLogin, SecretKey keyToken, byte[] saltToken) throws NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Timestamp createdAt = getCreatedAt(keyToken);
        return createdAt.equals(lastLogin) && verifySigma(saltToken);
    }

    private String[] getDecryptedPayload(SecretKey keyToken) {
        try {
            String[] parts = this.getPayload().split(",");
            if (parts.length != 2) {
                throw new InvalidTokenFormatException("The payload does not contain exactly one comma");
            }
            cipher.init(Cipher.DECRYPT_MODE, keyToken);
            byte[] decodedPayload = cipher.doFinal(ServerUtils.toByteArray(parts[0]));
            return BytesUtils.toString(decodedPayload).split("_");
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Decrypt Token Payload!");
    }
}
