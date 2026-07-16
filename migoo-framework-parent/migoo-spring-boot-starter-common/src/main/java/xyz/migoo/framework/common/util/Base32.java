package xyz.migoo.framework.common.util;

import java.util.Arrays;

/**
 * Base32 编解码工具类
 *
 * @author migoo
 * @since 1.3.16
 */
public final class Base32 {

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
    private static final int[] DECODE = new int[128];

    static {
        Arrays.fill(DECODE, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            DECODE[ALPHABET[i]] = i;
            DECODE[Character.toLowerCase(ALPHABET[i])] = i;
        }
    }

    private Base32() {
    }

    /**
     * 编码 byte 数组为 Base32 字符串
     */
    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        int index = 0;

        int currentByte;
        int lastByte;

        for (int i = 0; i < data.length; ) {
            currentByte = data[i] & 0xFF;
            if (index > 3) {
                if (i + 1 < data.length) {
                    lastByte = data[++i] & 0xFF;
                } else {
                    lastByte = 0;
                }
                result.append(ALPHABET[(currentByte << (8 - index) | lastByte >> index) & 0x1F]);
                index = (index + 5) % 8;
            } else {
                result.append(ALPHABET[(currentByte >> (3 - index)) & 0x1F]);
                index = (index + 5) % 8;
                if (index == 0) {
                    i++;
                }
            }
        }

        return result.toString();
    }

    /**
     * 解码 Base32 字符串为 byte 数组
     */
    public static byte[] decode(String base32) {
        if (base32 == null || base32.isEmpty()) {
            return new byte[0];
        }

        base32 = base32.replaceAll("[\\s=]", "").toUpperCase();
        int numBytes = base32.length() * 5 / 8;
        byte[] result = new byte[numBytes];

        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (char c : base32.toCharArray()) {
            if (c >= DECODE.length || DECODE[c] == -1) {
                continue;
            }
            buffer <<= 5;
            buffer |= DECODE[c];
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                result[count++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }

        if (count < numBytes) {
            byte[] trimmed = new byte[count];
            System.arraycopy(result, 0, trimmed, 0, count);
            return trimmed;
        }
        return result;
    }
}
