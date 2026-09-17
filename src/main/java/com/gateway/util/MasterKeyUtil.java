package com.gateway.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MasterKeyUtil {

    private static final String ALGORITHM = "AES";
    private static final String MASTER_KEY_ENV = "MASTER_KEY";
    private static final SecretKeySpec masterKey = createMasterKey();

    private static SecretKeySpec createMasterKey() {
        String key = System.getenv(MASTER_KEY_ENV);
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalStateException("MASTER_KEY environment variable is required.");
        }

        if (key.length() != 16) {
            throw new IllegalStateException("MASTER_KEY must be exactly 16 characters for AES-128.");
        }

        return new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    public static String encryptKey(SecretKey keyToEncrypt) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(keyToEncrypt.getEncoded()));
    }

    public static SecretKey decryptKey(String encryptedKeyString) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, masterKey);
        byte[] decryptedKeyBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedKeyString));
        return new SecretKeySpec(decryptedKeyBytes, ALGORITHM);
    }
}
