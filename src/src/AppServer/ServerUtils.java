package src.AppServer;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.KeyStore;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ServerUtils {

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss");

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
    
    public static byte[] encryptWithSalt(byte[] data, byte[] salt) throws NoSuchAlgorithmException{
        byte[] concatenation = concatByteArray(salt, data);
        return MessageDigest.getInstance("SHA-256").digest(concatenation);
    }

    public static boolean fileWrite(String path, String content) throws IOException {
        try (FileWriter fileWriter = new FileWriter(path)) {
            String fileContent = content;
            fileWriter.write(fileContent);
        } catch (IOException e) {
            return false;
        }
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
            SecretKey key = (SecretKey) keyStore.getKey(keyAlias, password.toCharArray());
            return key;
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public static Timestamp getNow() {
        Timestamp instant = Timestamp.from(Instant.now());
        return instant;
    }

    public static Timestamp maxTimestamp(Timestamp t1, Timestamp t2) {
        if (t1.compareTo(t2) > 0) {
            return t1;
        } else {
            return t2;
        }
    }

    public static Timestamp minTimestamp(Timestamp t1, Timestamp t2) {
        if (t1.compareTo(t2) > 0) {
            return t2;
        } else {
            return t1;
        }
    }

    public static int diffTimestampMillis(Timestamp t1, Timestamp t2)  {
        long timeT1 = t1.getTime();
        long timeT2 = t2.getTime();
        long diff = Math.round(Math.abs(timeT1 - timeT2));
        return (int) diff;
    }

    public static Timestamp addMillis(Timestamp startDate, int millis){
        LocalDateTime cEndDate = startDate.toLocalDateTime().plusNanos(millis * 1000);
        String strCEndDate = cEndDate.format(formatter);
        return Timestamp.valueOf(strCEndDate);
    }

    public static Timestamp minusMillis(Timestamp startDate, int millis){
        LocalDateTime cEndDate = startDate.toLocalDateTime().minusNanos(millis * 1000);
        String strCEndDate = cEndDate.format(formatter);
        return Timestamp.valueOf(strCEndDate);
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

}
