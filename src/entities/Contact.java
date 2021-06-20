/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entities;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Arrays;
import src.AppServer.ServerUtils;

/**
 *
 * @author marco
 */
public class Contact extends ContactReport {

    public Contact(byte[] reporterHashedCf, byte[] reportedHashedCf, int duration, Timestamp startDate) {
        super(reporterHashedCf, reportedHashedCf, duration, startDate);
    }

    public Contact(ContactReport report) {
        super(report.getReporterHashedCf(), report.getReportedHashedCf(), report.getDuration(), report.getStartDate());
    }

    public byte[] getOtherUserHashedCf(byte[] notThisHashedCf) {
        if (Arrays.equals(this.getReporterHashedCf(), notThisHashedCf)) {
            if (!Arrays.equals(this.getReportedHashedCf(), notThisHashedCf)) {
                return this.getReportedHashedCf();
            }
            return null;
        }
        if (Arrays.equals(this.getReportedHashedCf(), notThisHashedCf)) {
            if (!Arrays.equals(this.getReporterHashedCf(), notThisHashedCf)) {
                return this.getReporterHashedCf();
            }
            return null;
        }
        return null;
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
        return this.getDuration() == that.getDuration() && ((Arrays.equals(getReporterHashedCf(), that.getReportedHashedCf()) && Arrays.equals(getReportedHashedCf(), that.getReportedHashedCf()))
                || (Arrays.equals(getReporterHashedCf(), that.getReportedHashedCf()) && Arrays.equals(getReportedHashedCf(), that.getReporterHashedCf()))) && getStartDate().equals(that.getStartDate());
    }

    @Override
    public String toString() {
        return MessageFormat.format("[Contact: {0}, {1}, {2}, {3}]", ServerUtils.toString(this.getReporterHashedCf()), ServerUtils.toString(this.getReportedHashedCf()), this.getDuration(), this.getStartDate());
    }

}
