package src.AppClient;

import java.util.Objects;

public class Seed {
    private final long genDate;
    private final byte[] value;

    public Seed(long genDate, byte[] value) {
        this.genDate = genDate;
        this.value = value;
    }

    public long getGenDate() {
        return genDate;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seed seed = (Seed) o;
        return genDate == seed.genDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(genDate);
    }
}
