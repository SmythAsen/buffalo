package com.asen.buffalo.digest;

import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * hash算法工具
 *
 * @author Asen
 * @since 1.1.1
 */
@Slf4j
public class HashUtils {

    public static String md5(String str) {
        return compute(str, "MD5");
    }

    public static String sha1(String str) {
        return compute(str, "SHA-1");
    }

    private static String compute(String inStr, String hash) {
        String result = null;
        try {
            byte[] valueByte = inStr.getBytes();
            MessageDigest md = MessageDigest.getInstance(hash);
            md.update(valueByte);
            result = toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    private static String toHex(byte[] buffer) {
        StringBuilder sb = new StringBuilder(buffer.length * 2);
        for (byte b : buffer) {
            sb.append(Character.forDigit((b & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(b & 0x0f, 16));
        }
        return sb.toString();
    }
}
