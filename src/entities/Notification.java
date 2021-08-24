/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import core.tokens.NotificationToken;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 */
public class Notification {

    /*private final NotificationToken token;
    private Timestamp suspensionDate;

    public Notification(String code) throws InvalidKeyException, NoSuchAlgorithmException {
        this.token = new NotificationToken(code);
    }
    
    public Notification(NotificationToken token) throws InvalidKeyException, NoSuchAlgorithmException {
        this.token = token;
    }

    public NotificationToken getToken() {
        return token;
    }

    public Timestamp getExpireDate() {
        return token.getExpireDate();
    }

    public Timestamp getSuspensionDate() {
        return suspensionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Notification that = (Notification) o;
        return suspensionDate.equals(that.suspensionDate) && token.equals(that.getToken());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.token);
        hash = 83 * hash + Objects.hashCode(this.suspensionDate);
        return hash;
    }

    public int getId() {
        return token.getId();
    }

    public void setSuspensionDate(Timestamp suspensionDate) {
        this.suspensionDate = suspensionDate;
    }*/
}
