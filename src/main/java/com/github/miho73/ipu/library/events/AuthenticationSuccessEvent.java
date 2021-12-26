package com.github.miho73.ipu.library.events;

import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServletRequest;

public class AuthenticationSuccessEvent extends ApplicationEvent {
    public AuthenticationSuccessEvent(HttpServletRequest source) {
        super(source);
    }
}
