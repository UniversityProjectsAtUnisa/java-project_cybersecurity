package core.tokens;

import exceptions.InvalidTokenFormatException;
import utils.BytesUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Arrays;

/**
 * AES(KEYtoken, “CF, data_creazione”).HMAC(AES(KEYtoken, “CF, data_creazione”), SALTtoken)
 */
public final class AuthToken extends Token {
    private final byte[] iv;

    public AuthToken(byte[] hashedCf, SecretKey keyToken, byte[] saltToken, Timestamp creationDate, byte[] iv) throws NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        super(AuthToken.getEncryptedToken(hashedCf, keyToken, creationDate, iv), saltToken);
        this.iv = iv;
    }

    public static AuthToken createToken(byte[] hashedCf, SecretKey keyToken, byte[] saltToken, Timestamp creationDate, SecureRandom sr) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] newIv = new byte[getCipher().getBlockSize()];
        sr.nextBytes(newIv);
        return new AuthToken(hashedCf, keyToken, saltToken, creationDate, newIv);
    }

    private static Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException {
        return Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    public byte[] getCfToken(SecretKey keyToken) throws Exception {
        byte[] decodedPayload = getDecryptedPayload(keyToken);
        return Arrays.copyOfRange(decodedPayload, 0, decodedPayload.length - Long.BYTES);
    }

    public Timestamp getCreatedAt(SecretKey keyToken) throws Exception {
        byte[] decodedPayload = getDecryptedPayload(keyToken);
        int splitIndex = decodedPayload.length - Long.BYTES;
        byte[] rawCreationDate = Arrays.copyOfRange(decodedPayload, splitIndex, decodedPayload.length);
        return new Timestamp(BytesUtils.toLong(rawCreationDate));
    }

    @Override
    public String toString() {
        return "AuthToken: " + super.toString();
    }

    public boolean isValid(Timestamp lastLogin, SecretKey keyToken, byte[] saltToken) throws Exception {
        Timestamp createdAt = getCreatedAt(keyToken);
        return lastLogin.getTime() == createdAt.getTime() && verifySigma(saltToken);
    }

    public static byte[] getEncryptedToken(byte[] hashedCf, SecretKey keyToken, Timestamp creationDate, byte[] iv) throws InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        byte[] payloadStr = BytesUtils.concat(hashedCf, BytesUtils.fromLong(creationDate.getTime()));
        Cipher cipher = getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, keyToken, new IvParameterSpec(iv));
        return cipher.doFinal(payloadStr);
    }

    private byte[] getDecryptedPayload(SecretKey keyToken) throws NoSuchAlgorithmException, NoSuchPaddingException {
        try {
            Cipher cipher = getCipher();
            cipher.init(Cipher.DECRYPT_MODE, keyToken, new IvParameterSpec(iv));
            byte[] decodedPayload = cipher.doFinal(getPayload());
            if (decodedPayload.length - Long.BYTES < 0) {
                throw new InvalidTokenFormatException("The payload length is not valid");
            }
            return decodedPayload;
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Decrypt Token Payload!");
    }
}
