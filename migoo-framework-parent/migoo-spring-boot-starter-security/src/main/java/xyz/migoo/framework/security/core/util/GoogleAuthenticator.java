package xyz.migoo.framework.security.core.util;

import cn.hutool.core.codec.Base32;
import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * @author xiaomi
 * @date 2018/8/17 16:08
 */
public class GoogleAuthenticator {

    private final static GoogleAuthenticator GOOGLE_AUTHENTICATOR = new GoogleAuthenticator();
    /**
     * taken from Google pam docs - we probably don't need to mess with these
     */

    private static final int SECRET_SIZE = 10;
    private static final String SEED = "g8GjEvTbW5oVSV7avLBdwIHqGlUYNzKFI7izOF8GwLDVKs2m0QN7vxRs2im5MDaNCWGmcD2rvcZx";
    private static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";
    private static final int SCRATCH_CODE_LENGTH = 8;
    private static final int BYTES_PER_SCRATCH_CODE = 4;

    private GoogleAuthenticator() {
    }

    /**
     * enter the code shown on device. Edit this and run it fast before the code expires!
     * should give 5 * 30 seconds of grace...
     *
     * @param secretKey authenticator secret key
     * @param code      authenticator code
     * @return verify result true / false
     */
    @SneakyThrows
    public static boolean verify(String secretKey, String code) {
        return GOOGLE_AUTHENTICATOR.verify(secretKey, Long.parseLong(code), System.currentTimeMillis());
    }

    @SneakyThrows
    public static String generateSecretKey() {
        SecureRandom sr = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM);
        sr.setSeed(Base64.getDecoder().decode(SEED));
        return Base32.encode(sr.generateSeed(SECRET_SIZE));
    }

    private boolean verify(String secretKey, long code, long timeMsec) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] decodedKey = Base32.decode(secretKey);
        // convert unix msec time into a 30 second "window"
        // this is per the TOTP spec (see the RFC for details)
        long t = (timeMsec / 1000L) / 30L;
        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.
        /*
         * default 3 - max 17 (from google docs)最多可偏移的时间
         */
        int windowSize = 3;
        for (int i = -windowSize; i <= windowSize; ++i) {
            if (verifyCode(decodedKey, t + i) == code) {
                return true;
            }
        }
        return false;
    }

    private int verifyCode(byte[] key, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = SCRATCH_CODE_LENGTH; i-- > 0; value >>>= SCRATCH_CODE_LENGTH) {
            data[i] = (byte) value;
        }
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);
        int offset = hash[20 - 1] & 0xF;
        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < BYTES_PER_SCRATCH_CODE; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;
        return (int) truncatedHash;
    }
}


