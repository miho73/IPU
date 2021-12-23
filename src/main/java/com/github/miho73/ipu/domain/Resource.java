package com.github.miho73.ipu.domain;

import java.sql.Timestamp;

public class Resource {
    private String resource_code, registered_by, resource_name;
    private Timestamp registered;

    public String getResource_code() {
        return resource_code;
    }
    public void setResource_code(String resource_code) {
        this.resource_code = resource_code;
    }

    public String getRegistered_by() {
        return registered_by;
    }
    public void setRegistered_by(String registered_by) {
        this.registered_by = registered_by;
    }

    public String getResource_name() {
        return resource_name;
    }
    public void setResource_name(String resource_name) {
        this.resource_name = resource_name;
    }

    public Timestamp getRegistered() {
        return registered;
    }
    public void setRegistered(Timestamp registered) {
        this.registered = registered;
    }
}
