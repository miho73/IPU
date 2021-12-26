package com.github.miho73.ipu.library.events;

import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationFailureBadCredentialsEvent extends ApplicationEvent {
    public AuthenticationFailureBadCredentialsEvent(HttpServletRequest source) {
        super(source);
    }
}
