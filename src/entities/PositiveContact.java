package entities;

import src.AppServer.ServerUtils;

import java.sql.Timestamp;
import java.util.HashMap;

public class PositiveContact {

    private byte[] seed;
    private Timestamp seedCreationDate;
    private HashMap<byte[], Integer> detectedCodes = new HashMap<>();

    public PositiveContact(byte[] seed, Timestamp seedCreationDate, HashMap<byte[], Integer> detectedCodes) {
        this.seed = seed;
        this.seedCreationDate = seedCreationDate;
        this.detectedCodes = detectedCodes;
    }

    public PositiveContact(byte[] seed, HashMap<byte[], Integer> detectedCodes) {
        this.seed = seed;
        this.seedCreationDate = ServerUtils.getNow();
        this.detectedCodes = detectedCodes;
    }

    public PositiveContact(byte[] seed, Timestamp seedCreationDate) {
        this.seed = seed;
        this.seedCreationDate = seedCreationDate;
    }

    public PositiveContact(byte[] seed) {
        this.seed = seed;
        this.seedCreationDate = ServerUtils.getNow();
    }

    public byte[] getSeed() {
        return seed;
    }

    public void setSeed(byte[] seed) {
        this.seed = seed;
    }

    public Timestamp getSeedCreationDate() {
        return seedCreationDate;
    }

    public void setSeedCreationDate(Timestamp seedCreationDate) {
        this.seedCreationDate = seedCreationDate;
    }

    public HashMap<byte[], Integer> getDetectedCodes() {
        return detectedCodes;
    }

    public void setDetectedCodes(HashMap<byte[], Integer> detectedCodes) {
        this.detectedCodes = detectedCodes;
    }
}
