/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppServer;

import core.tokens.NotificationToken;
import entities.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Database {

    private HashMap<byte[], User> users = new HashMap<>();
    private HashMap<byte[], Swab> swabs = new HashMap<>();
    private HashMap<byte[], PositiveContact> positiveContacts = new HashMap<>();

    // ----------------------------------------------------------------------------------------------------------------
    // USER

    public boolean addUser(byte[] hashedCf, byte[] password, byte[] passwordSalt, SecretKey keyInfo)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
            BadPaddingException, InvalidKeyException {
        User u = new User(hashedCf, password, passwordSalt, keyInfo);
        return users.putIfAbsent(hashedCf, u) == null;
    }

    public boolean removeUser(byte[] hashedCf) throws NoSuchPaddingException, IllegalBlockSizeException,
            NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        return users.remove(hashedCf) != null;
    }

    public User findUser(byte[] hashedCf) {
        return users.get(hashedCf);
    }

    public User updateUser(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt, Timestamp lastLoginDate,
                           Timestamp minimumSeedDate, Timestamp lastRiskRequestDate) {
        User user = findUser(hashedCf);
        if (user == null) {
            return null;
        }
        if (lastLoginDate != null) {
            user.setLastLoginDate(lastLoginDate);
        }
        if (hashedPassword != null && passwordSalt != null) {
            String[] info = user.getInfo().toString().split("_");
            info[0] = passwordSalt.toString();
            user.setHashedPassword(hashedPassword);
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        if (minimumSeedDate != null) {
            String[] info = user.getInfo().toString().split("_");
            info[1] = minimumSeedDate.toString();
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        if (lastRiskRequestDate != null) {
            String[] info = user.getInfo().toString().split("_");
            info[3] = lastRiskRequestDate.toString();
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        return user;
    }
    public User updateUser(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt, Timestamp lastLoginDate,
                           Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
                           boolean hadRequestSeed, boolean isPositive) {
        User user = findUser(hashedCf);
        if (user == null) {
            return null;
        }
        if (lastLoginDate != null) {
            user.setLastLoginDate(lastLoginDate);
        }
        if (hashedPassword != null && passwordSalt != null) {
            String[] info = user.getInfo().toString().split("_");
            info[0] = passwordSalt.toString();
            user.setHashedPassword(hashedPassword);
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        if (minimumSeedDate != null) {
            String[] info = user.getInfo().toString().split("_");
            info[1] = minimumSeedDate.toString();
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        if (lastRiskRequestDate != null) {
            String[] info = user.getInfo().toString().split("_");
            info[3] = lastRiskRequestDate.toString();
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        String[] info = user.getInfo().toString().split("_");
        info[2] = isPositive == true ? "true" : "false";
        info[4] = hadRequestSeed == true ? "true" : "false";
        user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        return user;
    }

    public User updateUser(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt, Timestamp lastLoginDate,
                           Timestamp minimumSeedDate, Timestamp lastRiskRequestDate, boolean isPositive) {
        User user = findUser(hashedCf);
        if (user == null) {
            return null;
        }
        if (lastLoginDate != null) {
            user.setLastLoginDate(lastLoginDate);
        }
        if (hashedPassword != null && passwordSalt != null) {
            String[] info = user.getInfo().toString().split("_");
            info[0] = passwordSalt.toString();
            user.setHashedPassword(hashedPassword);
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        if (minimumSeedDate != null) {
            String[] info = user.getInfo().toString().split("_");
            info[1] = minimumSeedDate.toString();
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }

        if (lastRiskRequestDate != null) {
            String[] info = user.getInfo().toString().split("_");
            info[3] = lastRiskRequestDate.toString();
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        String[] info = user.getInfo().toString().split("_");
        info[2] = isPositive == true ? "true" : "false";
        user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        return user;
    }

    public User updateUser(byte[] hashedCf, byte[] hashedPassword, byte[] passwordSalt, Timestamp lastLoginDate,
                           Timestamp minimumSeedDate, boolean hadRequestSeed, Timestamp lastRiskRequestDate) {
        User user = findUser(hashedCf);
        if (user == null) {
            return null;
        }
        if (lastLoginDate != null) {
            user.setLastLoginDate(lastLoginDate);
        }
        if (hashedPassword != null && passwordSalt != null) {
            String[] info = user.getInfo().toString().split("_");
            info[0] = passwordSalt.toString();
            user.setHashedPassword(hashedPassword);
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        if (minimumSeedDate != null) {
            String[] info = user.getInfo().toString().split("_");
            info[1] = minimumSeedDate.toString();
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        if (lastRiskRequestDate != null) {
            String[] info = user.getInfo().toString().split("_");
            info[3] = lastRiskRequestDate.toString();
            user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        }
        String[] info = user.getInfo().toString().split("_");
        info[4] = hadRequestSeed == true ? "true" : "false";
        user.setInfo(ServerUtils.toByteArray(String.join("_", info)));
        return user;
    }
    // END USER
    // ----------------------------------------------------------------------------------------------------------------


    // ----------------------------------------------------------------------------------------------------------------
    // SWAB

    public boolean createSwab(byte[] swabCode, Timestamp creationDate, boolean isUsed) {
        Swab s = new Swab(swabCode, creationDate, isUsed);
        return swabs.putIfAbsent(swabCode, s) == null;
    }

    public boolean createSwab(byte[] swabCode, boolean isUsed) {
        Swab s = new Swab(swabCode, ServerUtils.getNow(), isUsed);
        return swabs.putIfAbsent(swabCode, s) == null;
    }

    public boolean createSwab(byte[] swabCode) {
        Swab s = new Swab(swabCode, ServerUtils.getNow(), false);
        return swabs.putIfAbsent(swabCode, s) == null;
    }

    public boolean removeSwab(byte[] swabCode) {
        return swabs.remove(swabCode) != null;
    }

    public Swab findSwab(byte[] swabCode) {
        return swabs.get(swabCode);
    }

    public Swab updateSwab(byte[] swabCode, Timestamp creationDate, boolean isUsed) {
        Swab s = swabs.get(swabCode);
        s.setCreationDate(creationDate);
        s.setUsed(isUsed);
        return s;
    }

    public Swab updateSwab(byte[] swabCode, Timestamp creationDate) {
        Swab s = swabs.get(swabCode);
        s.setCreationDate(creationDate);
        return s;
    }

    // END SWAB
    // ----------------------------------------------------------------------------------------------------------------

    // CONTACTS
    // ----------------------------------------------------------------------------------------------------------------

    public boolean createPositiveContact(byte[] seed, Timestamp creationDate, HashMap<byte[], Integer> detectedCodes ){
        PositiveContact pc = new PositiveContact(seed, creationDate, detectedCodes);
        return positiveContacts.putIfAbsent(seed, pc) == null;
    }
    public boolean createPositiveContact(byte[] seed, HashMap<byte[], Integer> detectedCodes ){
        PositiveContact pc = new PositiveContact(seed, detectedCodes);
        return positiveContacts.putIfAbsent(seed, pc) == null;
    }

    public PositiveContact findPositiveContact(byte[] seed){
        return positiveContacts.get(seed);
    }

    public boolean removePositiveContact(byte[] seed){
        return positiveContacts.remove(seed) != null;
    }

    public Set<byte[]> getAllPositiveSeeds(){
        return positiveContacts.keySet();
    }

    // END CONTACTS
    // ----------------------------------------------------------------------------------------------------------------

}
