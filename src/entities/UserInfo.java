package entities;

import java.io.Serializable;
import java.sql.Timestamp;

public class UserInfo implements Serializable {

    /*
    * sale_password
    data_minima_semi(data minima a partire dalla quale mandiamo tutti i semi)
    data_ultima_richiesta_rischio
    ha_chiesto_semi
    is_positive

    *
    * */

    private byte[] passwordSalt = new byte[32];
    private Timestamp minimumSeedDate;
    private Timestamp lastRiskRequestDate;
    private boolean hadRequestSeed;
    private boolean isPositive;

    public UserInfo(byte[] passwordSalt, Timestamp minimumSeedDate, Timestamp lastRiskRequestDate, boolean hadRequestSeed, boolean isPositive) {
        this.passwordSalt = passwordSalt;
        this.minimumSeedDate = minimumSeedDate;
        this.lastRiskRequestDate = lastRiskRequestDate;
        this.hadRequestSeed = hadRequestSeed;
        this.isPositive = isPositive;
    }

    public UserInfo(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
        this.minimumSeedDate = null;
        this.lastRiskRequestDate = null;
        this.hadRequestSeed = false;
        this.isPositive = false;
    }

    public byte[] getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(byte[] passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public Timestamp getMinimumSeedDate() {
        return minimumSeedDate;
    }

    public void setMinimumSeedDate(Timestamp minimumSeedDate) {
        this.minimumSeedDate = minimumSeedDate;
    }

    public Timestamp getLastRiskRequestDate() {
        return lastRiskRequestDate;
    }

    public void setLastRiskRequestDate(Timestamp lastRiskRequestDate) {
        this.lastRiskRequestDate = lastRiskRequestDate;
    }

    public boolean isHadRequestSeed() {
        return hadRequestSeed;
    }

    public void setHadRequestSeed(boolean hadRequestSeed) {
        this.hadRequestSeed = hadRequestSeed;
    }

    public boolean isPositive() {
        return isPositive;
    }

    public void setPositive(boolean positive) {
        isPositive = positive;
    }
}
