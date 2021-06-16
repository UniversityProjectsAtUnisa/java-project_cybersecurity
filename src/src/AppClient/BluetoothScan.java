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
}
