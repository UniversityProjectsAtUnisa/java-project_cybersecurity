package utils;

import java.io.Serializable;
import java.sql.Timestamp;

public class ContactReportMessage implements Serializable {
    private final int idUserToReport;
    private final int duration;
    private final Timestamp startDate;

    public ContactReportMessage(int idUserToReport, int duration, Timestamp startDate) {
        this.idUserToReport = idUserToReport;
        this.duration = duration;
        this.startDate = startDate;
    }

    public int getIdUserToReport() {
        return idUserToReport;
    }

    public int getDuration() {
        return duration;
    }

    public Timestamp getStartDate() {
        return startDate;
    }
}
