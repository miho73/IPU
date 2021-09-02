package com.github.miho73.ipu.library;

import com.github.miho73.ipu.exceptions.InvalidInputException;

public class Operations {
    public static byte[] XOR(byte[] a, byte[] b) throws InvalidInputException {
        if(a.length != b.length) throw new InvalidInputException("Two arrays must have same length");
        int len = a.length;
        byte[] ret = new byte[len];
        for(int i=0; i<len; i++) {
            ret[i] = (byte)(a[i]^b[i]);
        }
        return ret;
    }
}