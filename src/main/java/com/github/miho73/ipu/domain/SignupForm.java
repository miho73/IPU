package com.github.miho73.ipu.domain;

public class SignupForm {
    private String name, id, password, invite, gToken;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getInvite() {
        return invite;
    }
    public void setInvite(String invite) {
        this.invite = invite;
    }

    public String getgToken() {
        return gToken;
    }
    public void setgToken(String gToken) {
        this.gToken = gToken;
    }
}
