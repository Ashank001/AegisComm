package com.gateway.util;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

public class SHA256Util {

    // Method to generate a SHA-256 hash of an input string
    public static String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to hexadecimal string (standard practice for hashing)
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            // Log the error but return a generic string to allow the application to proceed
            System.err.println("Error generating SHA-256 hash: " + e.getMessage());
            return "HASH_ERROR";
        }
    }
}