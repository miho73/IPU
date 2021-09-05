package com.github.miho73.ipu.domain;

public class ProblemListAPI {
    public int frm, len;

    public ProblemListAPI(int frm, int len) {
        this.frm = frm;
        this.len = len;
    }

    public int getFrm() {
        return frm;
    }
    public int getLen() {
        return len;
    }
}
