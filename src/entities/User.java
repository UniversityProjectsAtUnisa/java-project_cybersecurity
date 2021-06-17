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

    private final byte[] cf;
    private final int id;
    private byte[] password;
    private byte[] userSalt;
    private Timestamp lastLoginDate;
    private Timestamp lastPositiveSwabDate;
    private Timestamp lastSwabCreationDate;

    public User(int id, byte[] cf, byte[] password, byte[] userSalt) {
        this.cf = cf;
        this.id = id;
        this.password = password;
        this.userSalt = userSalt;
        this.lastLoginDate = null;
        this.lastSwabCreationDate = null;
        this.lastPositiveSwabDate = null;
    }

    public User(int id, byte[] cf, byte[] password, byte[] userSalt,
            Timestamp lastLoginDate, Timestamp lastSwabCreationDate,
            Timestamp lastPositiveSwabDate) {
        this.cf = cf;
        this.id = id;
        this.password = password;
        this.userSalt = userSalt;
        this.lastLoginDate = lastLoginDate;
        this.lastSwabCreationDate = lastSwabCreationDate;
        this.lastPositiveSwabDate = lastPositiveSwabDate;
    }

    public byte[] getCf() {
        return cf;
    }

    public int getId() {
        return id;
    }

    public byte[] getPassword() {
        return password;
    }

    public void setPassword(byte[] password) {
        this.password = password;
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
    public int compareTo(User u) {
        return this.getId() - u.getId();
    }
}
