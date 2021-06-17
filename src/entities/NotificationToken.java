/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 */
public class NotificationToken implements Comparable {
    private final String code;
    private Timestamp expireDate;
    private Timestamp suspensionDate;
    private final int id;

    public NotificationToken(String code, int id) {
        this.code = code;
        this.expireDate = this.getExpireDateFromCode(code);
        this.suspensionDate = null;
        this.id = id;
    }

    private Timestamp getExpireDateFromCode(String code) {
        try {
            String data = code.substring(code.indexOf(",") + 1, code.indexOf("."));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = dateFormat.parse(data);
            Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
            return timestamp;
        } catch (Exception e) {
            return null;
        }
    }

    public String getCode() {
        return code;
    }

    public Timestamp getExpireDate() {
        return expireDate;
    }

    public Timestamp getSuspensionDate() {
        return suspensionDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationToken that = (NotificationToken) o;
        return code.equals(that.code) && expireDate.equals(that.expireDate) && suspensionDate.equals(that.suspensionDate);
    }

    public int getId() {
        return id;
    }

    public void setSuspensionDate(Timestamp suspensionDate) {
        this.suspensionDate = suspensionDate;
    }

    @Override
    public int compareTo(Object o) {
        NotificationToken notificationToken = (NotificationToken) o;
        if(this == notificationToken){
            return 0;
        }else if(this.getId() > notificationToken.getId()){
            return 1;
        }else{
            return -1;
        }
    }
}
