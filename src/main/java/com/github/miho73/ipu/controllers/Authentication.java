package com.github.miho73.ipu.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Authentication {
    private final String CAPTCHA_V3_SITE_KEY = "6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u";
    private final String CAPTCHA_V2_SITE_KEY = "6LeoxLwaAAAAABC-oJu76Dt36Yb5K12Eu7a0pjD8";

    @GetMapping("login")
    public String login(Model model) {
        model.addAttribute("capt_site", CAPTCHA_V2_SITE_KEY);
        model.addAttribute("captcha_version", "v2");
        model.addAttribute("error_visible", "none");
        model.addAttribute("return", "/");
        return "auth/signin";
    }
}
