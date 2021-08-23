/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import src.AppServer.ServerUtils;
import utils.BytesUtils;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 */
public class User {

    private final byte[] hashedCf;
    private byte[] hashedPassword;
    private Timestamp lastLoginDate;
    private byte[] info;

    public User(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt,
                Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
                boolean hadRequestSeed, boolean isPositive,
                SecretKey keyInfo) {
        this.hashedCf = hashedCf;
        this.hashedPassword = hashedPassword;
        this.lastLoginDate = null;
        this.info = encryptInfo(passwordSalt, minimumSeedDate, lastRiskRequestDate, hadRequestSeed, isPositive, keyInfo);
    }
    public User(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt,
                Timestamp lastLoginDate, Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
                boolean hadRequestSeed, boolean isPositive,
                SecretKey keyInfo) {
        this.hashedCf = hashedCf;
        this.hashedPassword = hashedPassword;
        this.lastLoginDate = lastLoginDate;
        this.info = encryptInfo(passwordSalt, minimumSeedDate, lastRiskRequestDate, hadRequestSeed, isPositive, keyInfo);
    }

    public User(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt, SecretKey keyInfo) {
        this.hashedCf = hashedCf;
        this.hashedPassword = hashedPassword;
        this.info = encryptInfo(passwordSalt, null, null, false, false, keyInfo);
    }

    private byte[] encryptInfo(byte[] passwordSalt,
                               Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
                               boolean hadRequestSeed, boolean isPositive,
                               SecretKey keyInfo) {
        String info =
                BytesUtils.toString(passwordSalt) + "_" +
                (minimumSeedDate == null ? -1 : minimumSeedDate.getTime()) + "_" +
                isPositive + "_" +
                (lastRiskRequestDate == null ? -1 : lastRiskRequestDate.getTime()) + "_" +
                hadRequestSeed;

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keyInfo);
            return cipher.doFinal(ServerUtils.toByteArray(info));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("EncryptInfo Failed");
    }

    public String getDecryptedInfo(SecretKey keyInfo) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyInfo);
            byte[] data = cipher.doFinal(info);
            return new String(data, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Decrypt Info Failed");
    }

    public void setEncryptInfo(String info, SecretKey keyInfo) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keyInfo);
            this.info = cipher.doFinal(ServerUtils.toByteArray(info));
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("EncryptInfo Failed");
    }

    public byte[] getHashedCf() {
        return hashedCf;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public Timestamp getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Timestamp lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public byte[] getInfo() {
        return info;
    }

    public void setInfo(byte[] info) {
        this.info = info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Arrays.equals(hashedCf, user.getHashedCf());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hashedCf);
    }
}
