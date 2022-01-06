package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller("MainControl")
public class MainControl {
    @Autowired private SessionService sessionService;

    @GetMapping("")
    public String indexPage(Model model, HttpSession session) {
        sessionService.loadSessionToModel(session, model);
        return "index";
    }
}
