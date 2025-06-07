package com.gateway.util;

import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String plainPassword = "admin123";
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        System.out.println("Generated Hash: " + hashed);
    }
}
