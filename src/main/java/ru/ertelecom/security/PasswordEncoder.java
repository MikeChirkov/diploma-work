/*
  Class PasswordEncoder
  Class for encoding and decoding strings to use them later for security purposes.

  Creation date: 06/07/2018
  Author: Boyarshinov Roman

  Copyright (c) 2018 ЭР-Телеком Холдинг, All Rights Reserved.
 */

package ru.ertelecom.security;

import lombok.Getter;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Шифрование и дешифрования паролей.
 */
public class PasswordEncoder {

    private static final String ENCRYPTION_ALGORITHM = "AES";
    private static final int BLOCK_SIZE = 128; // block size is 128bits
    private @Getter static final String ENCRYPTION_KEY = "mPAH9MQ5lQbFcLFCyOVlHg==";

    /**
     * Generates secret key string
     *
     * @return Returns secret key in the type of string
     * @throws Exception
     */
    public static String generateKey()
            throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(BLOCK_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        String encodedKey;
        encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        return encodedKey;
    }

    /**
     * Encodes a string that represents password via AES
     *
     * @param password Password string that needs to be encoded
     * @param key      Secret key
     * @return Returns encoded password string
     * @throws Exception
     */
    public static String encryptPassword(String password, String key)
            throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ENCRYPTION_ALGORITHM);

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        byte[] passwordBytes = password.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, originalKey);
        byte[] encryptedBytes = cipher.doFinal(passwordBytes);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encryptedBytes);
    }

    /**
     * Decodes an encoded string that represents password via AES
     *
     * @param encryptedPassword Encrypted string that represents password
     * @param key               Secret key
     * @return Returns decoded password
     * @throws Exception
     */
    public static String decryptPassword(String encryptedPassword, String key)
            throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ENCRYPTION_ALGORITHM);

        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedPassword);
        cipher.init(Cipher.DECRYPT_MODE, originalKey);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        return new String(decryptedByte);
    }
}
