package com.boeing.jobstarter.utils;


import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    private static final String key = "86b31971c9A7EjPx"; // 128 bit key

    private static final String initVector = "43dB3Ea6398cA66f"; // 16 bytes IV

    public static String encrypt(String pwd) {
        if (pwd != null && pwd.length() != 0) {
            try {
                IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
                SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

                byte[] encrypted = cipher.doFinal(pwd.getBytes());

                return Base64.getEncoder().encodeToString(encrypted);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        if (encrypted != null && encrypted.length() != 0) {
            try {
                IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
                SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

                byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));

                return new String(original);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        return null;
    }
}
