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
import java.time.LocalDateTime;
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
        Timestamp origin = new Timestamp(0);
        this.info = encryptInfo(passwordSalt, origin , origin, false, false, keyInfo);
    }

    public byte[] getEncryptedInfo(SecretKey keyInfo) {
        return this.info;
    }

    public byte[] getDecryptedInfo(SecretKey keyInfo) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keyInfo);
            return cipher.doFinal(info);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Decrypt Info Failed");
    }

    private void setEncryptedInfo(byte[] encryptedInfo) {
        this.info = encryptedInfo;
    }

    public void setInfo(byte[] passwordSalt,
            Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
            boolean hadRequestSeed, boolean isPositive,
            SecretKey keyInfo) {
        this.setEncryptedInfo(this.encryptInfo(passwordSalt,
                minimumSeedDate,
                lastRiskRequestDate,
                hadRequestSeed,
                isPositive,
                keyInfo));
    }

    public void setDecryptedInfo(byte[] decryptedInfo, SecretKey keyInfo) {
        this.setEncryptedInfo(this.encryptInfo(decryptedInfo, keyInfo));
    }

    private byte[] encryptInfo(byte[] passwordSalt,
            Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
            boolean hadRequestSeed, boolean isPositive,
            SecretKey keyInfo) {

        byte[] minimumSeedDateBytes = BytesUtils.fromLong(minimumSeedDate.getTime());
        byte[] lastRiskRequestDateBytes = BytesUtils.fromLong(lastRiskRequestDate.getTime());

        byte[] decryptedInfo = BytesUtils.concat(passwordSalt,
                minimumSeedDateBytes,
                lastRiskRequestDateBytes,
                new byte[]{(byte) (hadRequestSeed ? 1 : 0)},
                new byte[]{(byte) (isPositive ? 1 : 0)});
        return this.encryptInfo(decryptedInfo, keyInfo);
    }

    private byte[] encryptInfo(byte[] decryptedInfo, SecretKey keyInfo) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keyInfo);
            return cipher.doFinal(decryptedInfo);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getPasswordSalt(SecretKey keyInfo) {
        return Arrays.copyOfRange(this.getDecryptedInfo(keyInfo), 0, 32);
    }

    public void setPasswordSalt(byte[] passwordSalt, SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        byte[] decryptedInfoWithoutOldPasswordSalt = Arrays.copyOfRange(decryptedInfo, 32, decryptedInfo.length);
        this.setDecryptedInfo(BytesUtils.concat(passwordSalt, decryptedInfoWithoutOldPasswordSalt), keyInfo);
    }

    public long getMinimumSeedDate(SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        return BytesUtils.toLong(Arrays.copyOfRange(decryptedInfo, 32, 32 + Long.BYTES));
    }

    public void setMinimumSeedDate(Timestamp minimumSeedDate, SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        byte[] firstDecryptedPart = Arrays.copyOfRange(decryptedInfo, 0, 32);
        byte[] lastDecryptedPart = Arrays.copyOfRange(decryptedInfo, 32 + Long.BYTES, decryptedInfo.length);

        byte[] minimumSeedDateBytes = BytesUtils.fromLong(minimumSeedDate.getTime());
        this.setDecryptedInfo(BytesUtils.concat(firstDecryptedPart, minimumSeedDateBytes, lastDecryptedPart), keyInfo);
    }

    public long getLastRiskRequestDate(SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        return BytesUtils.toLong(Arrays.copyOfRange(
                decryptedInfo,
                32 + Long.BYTES,
                decryptedInfo.length - 2
        ));
    }

    public void setLastRiskRequestDate(Timestamp lastRiskRequestDate, SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        byte[] firstDecryptedPart = Arrays.copyOfRange(decryptedInfo, 0, 32 + Long.BYTES);
        byte[] lastDecryptedPart = Arrays.copyOfRange(decryptedInfo, 32 + 2 * Long.BYTES, decryptedInfo.length);

        byte[] lastRiskRequestDateBytes = BytesUtils.fromLong(lastRiskRequestDate.getTime());
        this.setDecryptedInfo(BytesUtils.concat(firstDecryptedPart, lastRiskRequestDateBytes, lastDecryptedPart), keyInfo);
    }

    public boolean getHadRequestSeed(SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        return decryptedInfo[decryptedInfo.length - 2] == 1;
    }

    public void setHadRequestSeed(boolean hadRequestSeed, SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        decryptedInfo[decryptedInfo.length - 2] = (byte) (hadRequestSeed ? 1 : 0);
        this.setDecryptedInfo(decryptedInfo, keyInfo);
    }

    public boolean getIsPositive(SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        return decryptedInfo[decryptedInfo.length - 1] == 1;
    }

    public void setIsPositive(boolean isPositive, SecretKey keyInfo) {
        byte[] decryptedInfo = this.getDecryptedInfo(keyInfo);
        decryptedInfo[decryptedInfo.length - 1] = (byte) (isPositive ? 1 : 0);
        this.setDecryptedInfo(decryptedInfo, keyInfo);
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
