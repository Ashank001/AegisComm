package com.gateway.util;

import org.mindrot.jbcrypt.BCrypt;

public class BcryptTest {
    public static void main(String[] args) {
        String plainPassword = "admin123";
        String hashedPassword = "$2a$10$L4b2xGQmIlmIQzw3fyUncuBoNq0Evb4pp2vdBOFgGdK1G0UtbNY9O";

        boolean isMatch = BCrypt.checkpw(plainPassword, hashedPassword);

        if (isMatch) {
            System.out.println("✅ Password matches the hash!");
        } else {
            System.out.println("❌ Password does NOT match the hash!");
        }
    }
}
