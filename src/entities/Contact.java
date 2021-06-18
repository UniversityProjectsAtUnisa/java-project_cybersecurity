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

    public Contact(byte[] reporterId, byte[] reportedId, int duration, Timestamp startDate) {
        super(reporterId, reportedId, duration, startDate);
    }

    public Contact(ContactReport report) {
        super(report.getReporterId(), report.getReportedId(), report.getDuration(), report.getStartDate());
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
        return this.getDuration() == that.getDuration() && ((Arrays.equals(getReporterId(), that.getReportedId()) && Arrays.equals(getReportedId(), that.getReportedId()))
                || (Arrays.equals(getReporterId(), that.getReportedId()) && Arrays.equals(getReportedId(), that.getReporterId()))) && getStartDate().equals(that.getStartDate());
    }

    @Override
    public String toString() {
        return MessageFormat.format("[Contact: {0}, {1}, {2}, {3}]", ServerUtils.toString(this.getReporterId()), ServerUtils.toString(this.getReportedId()), this.getDuration(), this.getStartDate());
    }

}
