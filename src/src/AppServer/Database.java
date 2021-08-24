/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.AppServer;

import core.tokens.NotificationToken;
import entities.*;
import src.AppClient.CodePair;
import src.AppClient.Seed;
import utils.BytesUtils;

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

    private HashMap<String, User> users = new HashMap<>();
    private HashMap<String, Swab> swabs = new HashMap<>();
    private HashMap<String, PositiveContact> positiveContacts = new HashMap<>();

    // ----------------------------------------------------------------------------------------------------------------
    // USER

    public boolean addUser(byte[] hashedCf, byte[] password, byte[] passwordSalt, SecretKey keyInfo) {
        User u = new User(hashedCf, password, passwordSalt, keyInfo);
        return users.putIfAbsent(BytesUtils.toString(hashedCf), u) == null;
    }

    public User findUser(byte[] hashedCf) {
        return users.get(BytesUtils.toString(hashedCf));
    }

    public void updateUser(User user, Timestamp lastLoginDate, Timestamp minSeedDate, Timestamp lastRiskRequestDate, SecretKey keyInfo) {
        if (lastLoginDate != null) {
            user.setLastLoginDate(lastLoginDate);
        }
        if (minSeedDate != null) {
            user.setMinimumSeedDate(minSeedDate, keyInfo);
        }
        if (lastRiskRequestDate != null) {
            user.setLastRiskRequestDate(lastRiskRequestDate, keyInfo);
        }
    }

    public User updateUser(byte[] hashedCf, Timestamp lastLoginDate,
                           Timestamp minimumSeedDate, Timestamp lastRiskRequestDate, SecretKey keyInfo) {
        User user = findUser(hashedCf);
        if (user == null) return null;
        updateUser(user, lastLoginDate, minimumSeedDate, lastRiskRequestDate, keyInfo);
        return user;
    }

    public User updateUser(byte[] hashedCf, Timestamp lastLoginDate,
                           Timestamp minimumSeedDate, Timestamp lastRiskRequestDate,
                           boolean hadRequestSeed, boolean isPositive, SecretKey keyInfo) {
        User user = findUser(hashedCf);
        if (user == null) return null;
        updateUser(user, lastLoginDate, minimumSeedDate, lastRiskRequestDate, keyInfo);
        // UPDATE HAD_REQUEST_SEED AND IS_POSITIVE
        user.setIsPositive(isPositive, keyInfo);
        user.setHadRequestSeed(hadRequestSeed, keyInfo);
        return user;
    }

    public User updateUser(byte[] hashedCf, Timestamp lastLoginDate, Timestamp minimumSeedDate,
                           Timestamp lastRiskRequestDate, boolean isPositive, SecretKey keyInfo) {
        User user = findUser(hashedCf);
        if (user == null) return null;
        updateUser(user, lastLoginDate, minimumSeedDate, lastRiskRequestDate, keyInfo);
        // UPDATE IS_POSITIVE
        user.setIsPositive(isPositive, keyInfo);
        return user;
    }

    public User updateUser(byte[] hashedCf, Timestamp lastLoginDate, Timestamp minimumSeedDate,
                           boolean hadRequestSeed, Timestamp lastRiskRequestDate, SecretKey keyInfo) {
        User user = findUser(hashedCf);
        if (user == null) return null;
        updateUser(user, lastLoginDate, minimumSeedDate, lastRiskRequestDate, keyInfo);
        // UPDATE HAD_REQUEST_SEED
        user.setHadRequestSeed(hadRequestSeed, keyInfo);
        return user;
    }
    // END USER
    // ----------------------------------------------------------------------------------------------------------------


    // ----------------------------------------------------------------------------------------------------------------
    // SWAB

    public boolean createSwab(byte[] swabCode, Timestamp creationDate) {
        Swab s = new Swab(swabCode, creationDate, false);
        return swabs.putIfAbsent(BytesUtils.toString(swabCode), s) == null;
    }

    public boolean removeSwab(byte[] swabCode) {
        return swabs.remove(BytesUtils.toString(swabCode)) != null;
    }

    public Swab findSwab(byte[] swabCode) {
        return swabs.get(BytesUtils.toString(swabCode));
    }

    public Swab updateSwab(byte[] swabCode, boolean isUsed) {
        Swab s = swabs.get(BytesUtils.toString(swabCode));
        s.setUsed(isUsed);
        return s;
    }

    // END SWAB
    // ----------------------------------------------------------------------------------------------------------------

    // CONTACTS
    // ----------------------------------------------------------------------------------------------------------------

    public boolean createPositiveContact(byte[] seed, long creationDate, List<CodePair> detectedCodes){
        PositiveContact pc = new PositiveContact(seed, creationDate, detectedCodes);
        return positiveContacts.putIfAbsent(BytesUtils.toString(seed), pc) == null;
    }

    public PositiveContact findPositiveContact(byte[] seed){
        return positiveContacts.get(BytesUtils.toString(seed));
    }

    public List<PositiveContact> findPositiveContactByCode(byte[] code, long instant) {
        return positiveContacts
                .values()
                .stream()
                .filter(pc -> {
                    for (CodePair pair: pc.getDetectedCodes())
                        if (pair.getInstant() == instant && Arrays.equals(pair.getCode(), code))
                            return true;
                    return false;
                })
                .toList();
    }

    public boolean removePositiveContact(byte[] seed){
        return positiveContacts.remove(BytesUtils.toString(seed)) != null;
    }

    public LinkedList<Seed> getAllPositiveSeeds(){
        return new LinkedList<>(
                positiveContacts
                        .values()
                        .stream()
                        .map(pc -> new Seed(pc.getSeedCreationDate(), pc.getSeed()))
                        .toList()
        );
    }

    // END CONTACTS
    // ----------------------------------------------------------------------------------------------------------------
}
