package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.LoginForm;
import com.github.miho73.ipu.services.UserService;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Map;

@Controller("UserControl")
@RequestMapping("/login")
@PropertySource("classpath:/properties/secret.properties")
public class UserControl {
    @Value("${captcha.v3.sitekey}") private String CAPTCHA_V3_SITE_KEY;
    @Value("${captcha.v2.sitekey}") private String CAPTCHA_V2_SITE_KEY;

    private final UserService userService;
    private final Logger LOGGER = LoggerFactory.getLogger(UserControl.class);

    @Autowired
    public UserControl(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public String getLogin(@RequestParam(value = "ret", required = false, defaultValue = "/") String ret, Model model) {
        model.addAttribute("capt_site", CAPTCHA_V3_SITE_KEY);
        model.addAttribute("captcha_version", "v3");
        model.addAttribute("error_visible", "none");
        model.addAttribute("return", ret);
        model.addAttribute("error_text", "");
        return "auth/signin";
    }

    @PostMapping("")
    public String postLogin(@RequestParam(value = "ret", required = false, defaultValue = "/") String ret,
                            @ModelAttribute LoginForm loginForm,
                            Model model,
                            HttpSession session) throws IOException {
        model.addAllAttributes(Map.of(
                "capt_site", CAPTCHA_V3_SITE_KEY,
                "captcha_version", "v3",
                "error_visible", "true",
                "return", ret
        ));

        // Login form validator
        if(loginForm.getId().equals("") || loginForm.getPassword().equals("")) {
            model.addAttribute("error_text", "서버에서 처리를 거부했습니다.");
            return "auth/signin";
        }

        UserService.LOGIN_RESULT result;
        try {
            result = userService.checkLogin(loginForm, session);
        } catch (SQLException | InvalidInputException | NoSuchAlgorithmException | IllegalArgumentException e) {
            LOGGER.error("Login error. Form: id="+loginForm.getId()+", gVers="+loginForm.getgVers()+", gToken="+loginForm.getgToken());
            LOGGER.error(e.getMessage(), e);
            model.addAttribute("error_text", "문제가 발생했습니다. 잠시 후에 다시 시도해주세요.");
            return "auth/signin";
        }

        if(result == UserService.LOGIN_RESULT.OK) {
            return "redirect:"+ret;
        }
        else if(result == UserService.LOGIN_RESULT.CAPTCHA_FAILED) {
            model.addAllAttributes(Map.of(
                    "capt_site", CAPTCHA_V2_SITE_KEY,
                    "captcha_version", "v2",
                    "error_text", "CAPTCHA 인증에 실패했습니다."
            ));
            return "auth/signin";
        }
        else if(result == UserService.LOGIN_RESULT.BAD_PASSWORD || result == UserService.LOGIN_RESULT.ID_NOT_FOUND) {
            model.addAttribute("error_text", "다시 시도해주세요.");
            return "auth/signin";
        }
        model.addAttribute("error_text", "문제가 발생했습니다. 잠시 후에 다시 시도해주세요.");
        return "auth/signin";
    }
}
