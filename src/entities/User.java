/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import src.AppServer.ServerUtils;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
public class User {

    private final byte[] hashedCf;
    private byte[] hashedPassword;
    private Timestamp lastLoginDate;
    private byte[] info;

    Cipher cipher = Cipher.getInstance("AES/CRC/PKCS5Padding");

    public User(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt,
                Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
                boolean hadRequestSeed, boolean isPositive,
                SecretKey keyInfo) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException {
        this.hashedCf = hashedCf;
        this.hashedPassword = hashedPassword;
        this.lastLoginDate = null;
        this.info = encryptInfo(passwordSalt, minimumSeedDate, lastRiskRequestDate, hadRequestSeed, isPositive, keyInfo);
    }
    public User(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt,
                Timestamp lastLoginDate, Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
                boolean hadRequestSeed, boolean isPositive,
                SecretKey keyInfo) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException {
        this.hashedCf = hashedCf;
        this.hashedPassword = hashedPassword;
        this.lastLoginDate = lastLoginDate;
        this.info = encryptInfo(passwordSalt, minimumSeedDate, lastRiskRequestDate, hadRequestSeed, isPositive, keyInfo);
    }

    public User(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt, SecretKey keyInfo)
            throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException {
        this.hashedCf = hashedCf;
        this.hashedPassword = hashedPassword;
        this.lastLoginDate = lastLoginDate;
        this.info = encryptInfo(passwordSalt, null, null, false, false, keyInfo);
    }

    private byte[] encryptInfo(byte[] passwordSalt,
                               Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
                               boolean hadRequestSeed, boolean isPositive,
                               SecretKey keyInfo) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        String info =
                passwordSalt.toString() + "_" +
                minimumSeedDate.toString() + "_" +
                isPositive + "_" +
                lastRiskRequestDate.toString() + "_" +
                hadRequestSeed;

        cipher.init(Cipher.ENCRYPT_MODE, keyInfo);
        return cipher.doFinal(ServerUtils.toByteArray(info));
    }

    public byte[] getHashedCf() {
        return hashedCf;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
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
        return hashedCf == user.getHashedCf();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hashedCf);
    }

}
