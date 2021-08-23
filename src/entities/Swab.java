package entities;

import src.AppServer.ServerUtils;

import java.sql.Timestamp;

public class Swab {

    private byte[] swabCode;
    private Timestamp creationDate;
    private boolean isUsed;

    public Swab(byte[] swabCode, Timestamp creationDate, boolean isUsed) {
        this.swabCode = swabCode;
        this.creationDate = creationDate;
        this.isUsed = isUsed;
    }

    public Swab(byte[] swabCode) {
        this.swabCode = swabCode;
        this.creationDate = ServerUtils.getNow();
        this.isUsed = false;
    }

    public byte[] getSwabCode() {
        return swabCode;
    }

    public void setSwabCode(byte[] swabCode) {
        this.swabCode = swabCode;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }
}
