package src.AppClient;

public class FakeInput {
    private static int index = 1;

    public static Credentials getNextCredential() {
        Credentials cred = new Credentials("cf"+index, "password"+index);
        index++;
        return cred;
    }
}
