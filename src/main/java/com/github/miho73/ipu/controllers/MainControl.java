package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller("MainControl")
public class MainControl {
    private SessionService sessionService;

    @Autowired
    public MainControl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("")
    public String indexPage(Model model, HttpSession session) {
        sessionService.loadSessionToModel(session, model);
        return "index";
    }
}
