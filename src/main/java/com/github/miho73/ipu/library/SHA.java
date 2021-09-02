package com.github.miho73.ipu.library;

import com.github.miho73.ipu.exceptions.InvalidInputException;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Base64.Decoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class SHA {
    private final int HASH_REPEAT = 12345;

    public String SHA512(String msg, String salt) throws NoSuchAlgorithmException, InvalidInputException {
        byte[] msgByte = Base64.getDecoder().decode(msg);
        byte[] saltByte = Base64.getDecoder().decode(salt);

        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.reset();
        byte[] nutrient;
        for(int i=0; i<HASH_REPEAT; i++) {
            if(i==0) nutrient = msgByte;
            else nutrient = Operations.XOR(msgByte, saltByte);
            md.update(nutrient);
            msgByte=md.digest();
        }
        return Base64.getEncoder().encodeToString(msgByte);
    }
}
