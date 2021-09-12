package com.github.miho73.ipu.library.security;

import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class SecureTools {
    public static byte[] getSecureRandom(int len) throws NoSuchAlgorithmException {
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[len];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
