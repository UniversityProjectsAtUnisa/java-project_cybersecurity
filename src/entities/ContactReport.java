/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import src.AppServer.ServerUtils;
import exceptions.InvalidContactException;
import java.text.MessageFormat;
import java.util.Objects;

/**
 *
 * @author marco
 */
public class ContactReport implements Comparable<ContactReport> {

    private final byte[] reporterId;
    private final byte[] reportedId;
    private final int duration;
    private final Timestamp startDate;

    public ContactReport(byte[] reporterId, byte[] reportedId, int duration, Timestamp startDate) {
        if (Arrays.equals(reporterId, reportedId)) {
            throw new InvalidContactException("Reporter and reported cannot be the same user");
        }
        this.reporterId = reporterId;
        this.reportedId = reportedId;
        this.duration = duration;
        this.startDate = startDate;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getEndDate() {
        return ServerUtils.addMillis(startDate, duration);
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

    public ContactReport findOverlapWith(ContactReport other) {
        // Se non sono sovrapposti ritorna null
        if (!this.getEndDate().after(other.getStartDate()) || !this.getStartDate().after(other.getEndDate())) {
            return null;
        }

        // Sono sicuramente sovrapposti quindi creo un nuovo contactReport che è il risultato della sovrapposizione
        Timestamp startDate = Collections.max(Arrays.asList(this.getStartDate(), other.getStartDate()));
        Timestamp endDate = Collections.min(Arrays.asList(this.getEndDate(), other.getEndDate()));
        
        System.out.println("startDate: "+startDate+" endDate: "+endDate + " duration: "+ ServerUtils.diffTimestampMillis(endDate, startDate));

        return new ContactReport(this.getReporterId(), this.getReportedId(), ServerUtils.diffTimestampMillis(endDate, startDate), startDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContactReport that = (ContactReport) o;
        return duration == that.duration && Arrays.equals(reporterId, that.reporterId) && Arrays.equals(reportedId, that.reportedId) && startDate.equals(that.startDate);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Arrays.hashCode(this.reporterId);
        hash = 61 * hash + Arrays.hashCode(this.reportedId);
        hash = 61 * hash + this.duration;
        hash = 61 * hash + Objects.hashCode(this.startDate);
        return hash;
    }

    @Override
    public int compareTo(ContactReport c) {
        if (this == c) {
            return 0;
        } else {
            return this.getStartDate().compareTo(c.startDate);
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format("reporterId={0}, reportedId={1}, duration={2}, startDate={3}", ServerUtils.toString(reporterId), ServerUtils.toString(reportedId), duration, startDate);
    }
}
