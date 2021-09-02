package com.github.miho73.ipu.domain;

public class LoginForm {
    private String id;
    private String password;
    private String gToken;
    private String gVers;

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
    public String getgToken() {
        return gToken;
    }
    public void setgToken(String gToken) {
        this.gToken = gToken;
    }
    public String getgVers() {
        return gVers;
    }
    public void setgVers(String gVers) {this.gVers = gVers;
    }
}
