package src.AppClient;

public class BluetoothScan {
    private final byte[] code;
    private final double distance;

    public BluetoothScan(byte[] code, double distance) {
        this.code = code;
        this.distance = distance;
    }

    public byte[] getCode() {
        return code;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return "BluetoothScan{id=" + code + ", distance=" + distance + '}';
    }
}
