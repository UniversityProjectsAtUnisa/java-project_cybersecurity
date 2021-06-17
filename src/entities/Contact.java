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
public class Contact extends ContactReport {

    public Contact(byte[] reporterId, byte[] reportedId, int duration, Timestamp startContactDate) {
        super(reporterId, reportedId, duration, startContactDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactReport that = (ContactReport) o;
        return this.getDuration() == that.getDuration() && ((Arrays.equals(getReporterId(), that.getReportedId()) && Arrays.equals(getReportedId(), that.getReportedId())) ||
                (Arrays.equals(getReporterId(), that.getReportedId()) && Arrays.equals(getReportedId(), that.getReporterId()))) && getStartContactDate().equals(that.getStartContactDate());
    }
}
