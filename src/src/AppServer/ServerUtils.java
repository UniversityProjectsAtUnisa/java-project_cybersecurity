package src.AppServer;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.KeyStore;

public class ServerUtils {

    public static String toHex(byte[] data, int length) {
        return toHex(data, data.length);
    }

    public static String toHex(byte[] data) {
        return toHex(data, data.length);
    }

    public static String toString(
            byte[] bytes,
            int length) {
        char[] chars = new char[length];

        for (int i = 0; i != chars.length; i++) {
            chars[i] = (char) (bytes[i] & 0xff);
        }

        return new String(chars);
    }


    public static String toString(
            byte[] bytes) {
        return toString(bytes, bytes.length);
    }

    public static byte[] toByteArray(
            String string) {
        byte[] bytes = new byte[string.length()];
        char[] chars = string.toCharArray();

        for (int i = 0; i != chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }

        return bytes;
    }

    public static byte[] concatByteArray(byte[] arr1, byte[] arr2) {
        byte[] concatArr = new byte[arr1.length + arr2.length];
        int pos = 0;
        for (byte element : arr1) {
            concatArr[pos] = element;
            pos++;
        }

        for (byte element : arr2) {
            concatArr[pos] = element;
            pos++;
        }
        return concatArr;
    }

    public static boolean fileWrite(String path, String content) throws IOException {
        FileWriter fileWriter = new FileWriter(path);
            String fileContent = content;
            fileWriter.write(fileContent);
            fileWriter.close();
            return true;
    }

    public static String fileRead(String path) throws IOException {
        String content = "";
        FileReader fileReader = new FileReader(path);
            int ch = fileReader.read();
            while (ch != -1) {
                content += ch;
                fileReader.close();
            }
            return content;
    }

    public static void storeToKeyStore(SecretKey keyToStore, String password, String filepath, String keyAlias) throws Exception{
        File file = new File(filepath);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        if(!file.exists()){
            keyStore.load(null,null);
        }
        keyStore.setKeyEntry(keyAlias,keyToStore,password.toCharArray(),null);
        OutputStream writeStream = new FileOutputStream(filepath);
        keyStore.store(writeStream,password.toCharArray());
    }

    public static SecretKey loadFromKeyStore(String filepath,String password,String keyAlias){
        try{
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream readStream = new FileInputStream(filepath);
            keyStore.load(readStream,password.toCharArray());
            SecretKey key = (SecretKey) keyStore.getKey(keyAlias,password.toCharArray());
            return key;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
