/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.util.Arrays;

/**
 *
 * @author marco
 */
public class ContactReport implements  Comparable{
    private final byte[] reporterId;
    private final byte[] reportedId;
    private final int duration;
    private final Timestamp startContactDate;


    public ContactReport(byte[] reporterId, byte[] reportedId, int duration, Timestamp startContactDate) {
        this.reporterId = reporterId;
        this.reportedId = reportedId;
        this.duration = duration;
        this.startContactDate = startContactDate;
    }

    public Timestamp getStartContactDate() {
        return startContactDate;
    }

    public int getDuration() {
        return duration;
    }

    public byte[] getReportedId() {
        return reportedId;
    }

    public byte[] getReporterId() {
        return reporterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactReport that = (ContactReport) o;
        return duration == that.duration && Arrays.equals(reporterId, that.reporterId) && Arrays.equals(reportedId, that.reportedId) && startContactDate.equals(that.startContactDate);
    }

    @Override
    public int compareTo(Object o) {
        ContactReport contactReport = (ContactReport) o;
        if(this == contactReport){
            return 0;
        }else{
            return this.getStartContactDate().compareTo(contactReport.startContactDate);
        }
    }
}
