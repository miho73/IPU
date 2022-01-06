package com.github.miho73.ipu.domain;

import java.sql.Timestamp;

public class User {
    private int code;
    private String id;
    private String name;
    private String pwd, salt;
    private String privilege;
    private String bio;
    private int experience;
    private String email;
    private Timestamp joined, lastLogin, lastSolve;

    private String inviteCode;

    public User() {}

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

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
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

    public String getInviteCode() {
        return inviteCode;
    }
    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getExperience() {
        return experience;
    }
    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getJoined() {
        return joined;
    }
    public void setJoined(Timestamp joined) {
        this.joined = joined;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }
    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Timestamp getLastSolve() {
        return lastSolve;
    }
    public void setLastSolve(Timestamp lastSolve) {
        this.lastSolve = lastSolve;
    }
}
