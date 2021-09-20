package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.services.DocsService;
import com.github.miho73.ipu.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller("DocsControl")
@RequestMapping("/docs")
public class DocsControl {
    private final SessionService sessionService;
    private final DocsService docsService;

    @Autowired
    public DocsControl(SessionService sessionService, DocsService docsService) {
        this.sessionService = sessionService;
        this.docsService = docsService;
    }

    @GetMapping("")
    public String docsList(Model model, HttpSession session) {
        sessionService.loadSessionToModel(session, model);
        return "docs/docs";
    }

    @GetMapping("{url}")
    public String docsReq(@PathVariable("url") String url, Model model, HttpSession session, HttpServletResponse response) throws IOException {
        System.out.println("dfff");
        String mapping = docsService.getMapping(url);
        if(mapping == null) {
            response.sendError(404);
            return null;
        }
        sessionService.loadSessionToModel(session, model);
        return mapping;
    }
}
