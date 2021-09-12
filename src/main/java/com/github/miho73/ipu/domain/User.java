package com.github.miho73.ipu.domain;

public class User {
    private long code;
    private String id;
    private String name;
    private String pwd, salt;
    private String privilege;
    private String last_solve;

    private String inviteCode;

    // General purpose
    public User(long code, String id, String name, String privilege, String last_solve) {
        this.code = code;
        this.id = id;
        this.name = name;
        this.privilege = privilege;
        this.last_solve = last_solve;
    }

    // Authentication purpose
    public User(long code, String id, String pwd, String salt) {
        this.code = code;
        this.id = id;
        this.pwd = pwd;
        this.salt = salt;
    }

    // Session purpose
    public User(String id, String name, String privilege) {
        this.id = id;
        this.name = name;
        this.privilege = privilege;
    }

    public User(String id, String name, String pwd, String inviteCode) {
        this.id = id;
        this.name = name;
        this.pwd = pwd;
        this.inviteCode = inviteCode;
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

    public String  getPrivilege() {
        return privilege;
    }
    public void setPrivilege(String  privilege) {
        this.privilege = privilege;
    }

    public String getLast_solve() {
        return last_solve;
    }
    public void setLast_solve(String last_solve) {
        this.last_solve = last_solve;
    }

    public String getInviteCode() {
        return inviteCode;
    }
    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
