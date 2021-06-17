package utils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class ContactReportMessage implements Serializable {
    private final int idUserToReport;
    private final int duration;
    private final Timestamp startDateTime;

    public ContactReportMessage(int idUserToReport, int duration, Timestamp startDateTime) {
        this.idUserToReport = idUserToReport;
        this.duration = duration;
        this.startDateTime = startDateTime;
    }

    public int getIdUserToReport() {
        return idUserToReport;
    }

    public int getDuration() {
        return duration;
    }

    public Timestamp getStartDateTime() {
        return startDateTime;
    }
}
