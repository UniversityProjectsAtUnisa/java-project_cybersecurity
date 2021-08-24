package core.tokens;

import src.AppServer.ServerUtils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import exceptions.InvalidTokenFormatException;
import exceptions.InvalidTokenSigmaException;
import utils.BytesUtils;

import java.util.Arrays;
import java.util.Objects;

public abstract class Token implements Serializable {

    private byte[] payload;
    private byte[] sigma;


    public Token(byte[] payload, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        this.payload = payload;
        this.sigma = calculateSigma(payload, key);
    }

    private byte[] calculateSigma(byte[] bytePayload, byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        Mac hMac = Mac.getInstance("HMacSHA256");
        Key hMacKey = new SecretKeySpec(key, "HMacSHA256");
        hMac.init(hMacKey);
        hMac.update(bytePayload);
        return hMac.doFinal();
    }

    public Token(byte[] raw) {
        int splitPoint = raw.length - 32;
        if (splitPoint < 0) {
            throw new InvalidTokenFormatException("The raw byte string length is not valid.");
        }
        this.payload = Arrays.copyOfRange(raw, 0, splitPoint);
        this.sigma = Arrays.copyOfRange(raw, splitPoint, raw.length);
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] getSigma() {
        return sigma;
    }

    public byte[] getCode() {
        return BytesUtils.concat(payload, sigma);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Token other = (Token) obj;
        if (!ServerUtils.secureByteCompare(this.payload, other.payload)) {
            return false;
        }
        if (!ServerUtils.secureByteCompare(this.sigma, other.sigma)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(getCode());
    }

    protected boolean verifySigma(byte[] key) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] calculatedSigma = this.calculateSigma(this.payload, key);
        return ServerUtils.secureByteCompare(calculatedSigma, this.sigma);
    }
}
