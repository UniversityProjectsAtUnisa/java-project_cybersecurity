package utils;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ContactReportMessage implements Serializable {
    private final int idUserToReport;
    private final int duration;
    private final LocalDateTime startDateTime;

    public ContactReportMessage(int idUserToReport, int duration, LocalDateTime startDateTime) {
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

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
}
