package src.AppClient;

import utils.Credentials;
import utils.SimulationData;

public class FakeInput {
    private static int index = 0;

    public static Credentials getNextCredential() {
        return SimulationData.VALID_CREDENTIALS[index++];
    }
}
