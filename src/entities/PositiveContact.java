package entities;

import src.AppClient.CodePair;
import src.AppServer.ServerUtils;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

public class PositiveContact {
    private final byte[] seed;
    private final long seedCreationDate;
    private final List<CodePair> detectedCodes;

    public PositiveContact(byte[] seed, long seedCreationDate, List<CodePair> detectedCodes) {
        this.seed = seed;
        this.seedCreationDate = seedCreationDate;
        this.detectedCodes = detectedCodes;
    }

    public byte[] getSeed() {
        return seed;
    }

    public long getSeedCreationDate() {
        return seedCreationDate;
    }

    public List<CodePair> getDetectedCodes() {
        return detectedCodes;
    }
}
