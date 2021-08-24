package entities;

import src.AppClient.CodePair;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class PositiveContact implements Serializable {
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

    @Override
    public String toString() {
        return "PositiveContact{" +
                "seed=" + Arrays.toString(seed) +
                ", seedCreationDate=" + seedCreationDate +
                ", detectedCodes=" + detectedCodes +
                '}';
    }
}
