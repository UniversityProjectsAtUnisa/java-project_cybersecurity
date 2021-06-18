package src.AppClient;

import utils.Credentials;
import utils.SimulationData;

public class FakeInput {
    private static int index = 0;

    public static Credentials getNextCredential() {
        String cf = SimulationData.VALID_CF_LIST.get(index++);
        String pass = "password-" + cf;
        return new Credentials(cf, pass);
    }
}
