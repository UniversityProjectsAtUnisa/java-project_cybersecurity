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
import java.util.Arrays;

/**
 * Auth: BASE64(id, data_creazione).HMACSHA256(BASE64(id, data_creazione),
 * sale_2)
 */
public final class AuthToken extends Token {

    public AuthToken(byte[] hashedCf, SecretKey keyToken, byte[] saltToken, Timestamp creationDate) throws NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException {
        super(AuthToken.getEncryptedToken(hashedCf, keyToken, creationDate), saltToken);
    }

    private static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("AES/ECB/PKCS5Padding");
    }

    public byte[] getCfToken(SecretKey keyToken) throws Exception {
        byte[] decodedPayload = getDecryptedPayload(keyToken);
        return Arrays.copyOfRange(decodedPayload, 0, decodedPayload.length - Long.BYTES);
    }

    public Timestamp getCreatedAt(SecretKey keyToken) throws Exception {
        byte[] decodedPayload = getDecryptedPayload(keyToken);
        int splitIndex = decodedPayload.length - Long.BYTES;
        byte[] rawCreationDate = Arrays.copyOfRange(decodedPayload, splitIndex, decodedPayload.length);
        return new Timestamp(BytesUtils.toLong(rawCreationDate));
    }

    @Override
    public String toString() {
        return "AuthToken: " + super.toString();
    }

    public boolean isValid(Timestamp lastLogin, SecretKey keyToken, byte[] saltToken) throws Exception {
        Timestamp createdAt = getCreatedAt(keyToken);
        return lastLogin.getTime() == createdAt.getTime() && verifySigma(saltToken);
    }

    static byte[] getEncryptedToken(byte[] hashedCf, SecretKey keyToken, Timestamp creationDate) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = getCipher();
        byte[] payloadStr = BytesUtils.concat(hashedCf, BytesUtils.fromLong(creationDate.getTime()));
        cipher.init(Cipher.ENCRYPT_MODE, keyToken);
        return cipher.doFinal(payloadStr);
    }

    private byte[] getDecryptedPayload(SecretKey keyToken) throws NoSuchAlgorithmException, NoSuchPaddingException {
        try {
            Cipher cipher = getCipher();
            cipher.init(Cipher.DECRYPT_MODE, keyToken);
            byte[] decodedPayload = cipher.doFinal(getPayload());
            if (decodedPayload.length - Long.BYTES < 0) {
                throw new InvalidTokenFormatException("The payload length is not valid");
            }
            return decodedPayload;
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Decrypt Token Payload!");
    }
}
