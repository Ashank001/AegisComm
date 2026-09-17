package com.gateway.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;

public class MasterKeyUtil {

    private static final String ALGORITHM = "AES";
    
    // A hardcoded master key for now. We will externalize this later for security.
    private static final String MASTER_KEY_STRING = "MasterKey16Bytes"; // Must be 16 characters for 128-bit AES
    private static SecretKeySpec masterKey;

    static {
        try {
            masterKey = new SecretKeySpec(MASTER_KEY_STRING.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Encrypts a key using the master key
    public static String encryptKey(SecretKey keyToEncrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(keyToEncrypt.getEncoded()));
    }

    // Decrypts a key using the master key
    public static SecretKey decryptKey(String encryptedKeyString) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        byte[] decryptedKeyBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedKeyString));
        return new SecretKeySpec(decryptedKeyBytes, ALGORITHM);
    }
}
