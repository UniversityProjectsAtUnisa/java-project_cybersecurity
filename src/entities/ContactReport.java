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
 * @author marco
 */
public class ContactReport {

    private final byte[] reporterHashedCf;
    private final byte[] reportedHashedCf;
    private final int duration;
    private final Timestamp startDate;

    public ContactReport(byte[] reporterHashedCf, byte[] reportedHashedCf, int duration, Timestamp startDate) {
        if (Arrays.equals(reporterHashedCf, reportedHashedCf)) {
            throw new InvalidContactException("Reporter and reported cannot be the same user");
        }
        this.reporterHashedCf = reporterHashedCf;
        this.reportedHashedCf = reportedHashedCf;
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

    public byte[] getReportedHashedCf() {
        return reportedHashedCf;
    }

    public byte[] getReporterHashedCf() {
        return reporterHashedCf;
    }

    public ContactReport findOverlapWith(ContactReport other) {
        // Se non sono sovrapposti ritorna null
        if (!this.getEndDate().after(other.getStartDate()) || !this.getStartDate().after(other.getEndDate())) {
            return null;
        }

        // Sono sicuramente sovrapposti quindi creo un nuovo contactReport che è il risultato della sovrapposizione
        Timestamp startDate = Collections.max(Arrays.asList(this.getStartDate(), other.getStartDate()));
        Timestamp endDate = Collections.min(Arrays.asList(this.getEndDate(), other.getEndDate()));

        System.out.println("startDate: " + startDate + " endDate: " + endDate + " duration: " + ServerUtils.diffTimestampMillis(endDate, startDate));

        return new ContactReport(this.getReporterHashedCf(), this.getReportedHashedCf(), ServerUtils.diffTimestampMillis(endDate, startDate), startDate);
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
        if (duration == that.duration &&
                Arrays.equals(reporterHashedCf, that.reporterHashedCf) &&
                Arrays.equals(reportedHashedCf, that.reportedHashedCf) &&
                startDate.equals(that.startDate)) {
            return true;
        }
        return false;
    }


//    @Override
//    public int compareTo(ContactReport that) {
//        int c = 0;
//        if (this == that ||
//                (duration == that.duration &&
//                        Arrays.equals(reporterHashedCf, that.reporterHashedCf) &&
//                        Arrays.equals(reportedHashedCf, that.reportedHashedCf) &&
//                        startDate.equals(that.startDate))) {
//            return 0;
//        } else {
//            c = this.getStartDate().compareTo(that.startDate);
//            if (c==0){
//
//            }
//            return c;
//        }
//    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Arrays.hashCode(this.reporterHashedCf);
        hash = 61 * hash + Arrays.hashCode(this.reportedHashedCf);
        hash = 61 * hash + this.duration;
        hash = 61 * hash + Objects.hashCode(this.startDate);
        return hash;
    }


    @Override
    public String toString() {
        return MessageFormat.format("reporterHashedCf={0}, reportedHashedCf={1}, duration={2}, startDate={3}", ServerUtils.toString(reporterHashedCf), ServerUtils.toString(reportedHashedCf), duration, startDate);
    }
}
