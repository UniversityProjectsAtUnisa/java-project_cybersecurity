package src.AppClient;

public class BluetoothScan {
    private final int id;
    private final double distance;

    public BluetoothScan(int id, double distance) {
        this.id = id;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BluetoothScan that = (BluetoothScan) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "BluetoothScan{id=" + id + ", distance=" + distance + '}';
    }
}
