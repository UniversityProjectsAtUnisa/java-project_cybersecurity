package src.AppServer;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;

import utils.BytesUtils;

public class ServerUtils {

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
    
    public static byte[] encryptWithSalt(byte[] data, byte[] salt) {
        byte[] concatenation = BytesUtils.concat(salt, data);
        try {
            return MessageDigest.getInstance("SHA-256").digest(concatenation);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(2);
        }
        throw new RuntimeException("NoSuchAlgorithmException in ServerUtils.encryptWithSalt");
    }

    public static void storeToKeyStore(SecretKey keyToStore, String password, String filepath, String keyAlias) throws Exception {
        File file = new File(filepath);
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        if (!file.exists()) {
            keyStore.load(null, null);
        } else {
            InputStream keyStoreData = new FileInputStream(filepath);
            keyStore.load(keyStoreData, password.toCharArray());
        }
        keyStore.setKeyEntry(keyAlias, keyToStore, password.toCharArray(), null);
        OutputStream writeStream = new FileOutputStream(filepath);
        keyStore.store(writeStream, password.toCharArray());
    }

    public static SecretKey loadFromKeyStore(String filepath, String password, String keyAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream readStream = new FileInputStream(filepath);
            keyStore.load(readStream, password.toCharArray());
            return (SecretKey) keyStore.getKey(keyAlias, password.toCharArray());
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public static Timestamp getNow() {
        return Timestamp.from(Instant.now());
    }

    public static boolean dumbStringCompare(String str1, String str2) {
        boolean flag = true;
        int index = 0;
        char[] charStr2 = str2.toCharArray();
        for (char c1 : str1.toCharArray()){
            flag = flag && (c1 == charStr2[index]);
            index ++;
        }
        return flag;
    }

    public static boolean secureByteCompare(byte[] bytes1, byte[] bytes2) {
        boolean flag = true;
        int index = 0;
        for (byte b1 : bytes1){
            flag = flag && (b1 == bytes2[index]);
            index ++;
        }
        return flag;
    }
}
