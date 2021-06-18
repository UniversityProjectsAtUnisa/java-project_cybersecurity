/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;

/**
 */
public class User implements Comparable<User> {

    private final byte[] hashedCf;
    private final int id;
    private byte[] hashedPassword;
    private byte[] userSalt;
    private Timestamp lastLoginDate;
    private Timestamp lastPositiveSwabDate;
    private Timestamp lastSwabCreationDate;

    
    public User(int id, byte[] hashedCf, byte[] hashedPassword, byte[] userSalt,
            Timestamp lastLoginDate, Timestamp lastSwabCreationDate,
            Timestamp lastPositiveSwabDate) {
        this.id = id;
        this.hashedCf = hashedCf;
        this.hashedPassword = hashedPassword;
        this.userSalt = userSalt;
        this.lastLoginDate = lastLoginDate;
        this.lastSwabCreationDate = lastSwabCreationDate;
        this.lastPositiveSwabDate = lastPositiveSwabDate;
    }
    
    public User(int id, byte[] hashedCf, byte[] hashedPassword, byte[] userSalt) {
        this(id, hashedCf, hashedPassword, userSalt, null, null, null);
    }

    public byte[] getHashedCf() {
        return hashedCf;
    }

    public int getId() {
        return id;
    }

    public byte[] getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(byte[] hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public byte[] getUserSalt() {
        return userSalt;
    }

    public void setUserSalt(byte[] userSalt) {
        this.userSalt = userSalt;
    }

    public Timestamp getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Timestamp lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Timestamp getLastPositiveSwabDate() {
        return lastPositiveSwabDate;
    }

    public void setLastPositiveSwabDate(Timestamp lastPositiveSwabDate) {
        this.lastPositiveSwabDate = lastPositiveSwabDate;
    }

    public Timestamp getLastSwabCreationDate() {
        return lastSwabCreationDate;
    }

    public void setLastSwabCreationDate(Timestamp lastSwabCreationDate) {
        this.lastSwabCreationDate = lastSwabCreationDate;
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
        return id == user.id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Arrays.hashCode(this.hashedCf);
        hash = 59 * hash + this.id;
        hash = 59 * hash + Arrays.hashCode(this.hashedPassword);
        hash = 59 * hash + Arrays.hashCode(this.userSalt);
        hash = 59 * hash + Objects.hashCode(this.lastLoginDate);
        hash = 59 * hash + Objects.hashCode(this.lastPositiveSwabDate);
        hash = 59 * hash + Objects.hashCode(this.lastSwabCreationDate);
        return hash;
    }

    @Override
    public int compareTo(User u) {
        return this.getId() - u.getId();
    }
}
