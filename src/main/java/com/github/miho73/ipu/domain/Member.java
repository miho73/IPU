package com.github.miho73.ipu.domain;

public class Member {
    private long code;
    private String id;
    private String name;
    private String pwd, salt;

    public Member(long code, String id, String name) {
        this.code = code;
        this.id = id;
        this.name = name;
    }

    public Member(long code, String id, String pwd, String salt) {
        this.code = code;
        this.id = id;
        this.pwd = pwd;
        this.salt = salt;
    }

    public String getPwd() {
        return pwd;
    }
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getSalt() {
        return salt;
    }
    public void setSalt(String salt) {
        this.salt = salt;
    }

    public long getCode() {
        return code;
    }
    public void setCode(long code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
