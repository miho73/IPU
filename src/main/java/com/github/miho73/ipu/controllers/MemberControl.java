package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.LoginForm;
import com.github.miho73.ipu.domain.Member;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.services.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

@Controller("MemberControl")
@RequestMapping("/login")
public class MemberControl {
    private final String CAPTCHA_V3_SITE_KEY = "6LefxLwaAAAAAOUYGq_6QLMbRMmGJZlQUQAcw-7u";
    private final String CAPTCHA_V2_SITE_KEY = "6LeoxLwaAAAAABC-oJu76Dt36Yb5K12Eu7a0pjD8";

    private final MemberService memberService;

    @Autowired
    public MemberControl(MemberService memberService) {
        this.memberService = memberService;
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
                                          Model model) throws IOException {
        // Login form validator
        if(loginForm.getId().equals("") || loginForm.getPassword().equals("")) {
            model.addAttribute("capt_site", CAPTCHA_V3_SITE_KEY);
            model.addAttribute("captcha_version", "v3");
            model.addAttribute("error_visible", "true");
            model.addAttribute("error_text", "서버에서 처리를 거부했습니다.");
            model.addAttribute("return", ret);
            return "auth/signin";
        }

        MemberService.LOGIN_RESULT result;
        try {
            result = memberService.checkLogin(loginForm);
        } catch (SQLException | InvalidInputException | NoSuchAlgorithmException e) {
            model.addAttribute("capt_site", CAPTCHA_V3_SITE_KEY);
            model.addAttribute("captcha_version", "v3");
            model.addAttribute("error_visible", "true");
            model.addAttribute("error_text", "문제가 발생했습니다. 잠시 후에 다시 시도해주세요.");
            model.addAttribute("return", ret);
            return "auth/signin";
        }

        if(result == MemberService.LOGIN_RESULT.OK) {
            return "redirect:"+ret;
        }
        else if(result == MemberService.LOGIN_RESULT.CAPTCHA_FAILED) {
            model.addAttribute("capt_site", CAPTCHA_V2_SITE_KEY);
            model.addAttribute("captcha_version", "v2");
            model.addAttribute("error_visible", "true");
            model.addAttribute("error_text", "CAPTCHA 인증에 실패했습니다.");
            model.addAttribute("return", ret);
            return "auth/signin";
        }
        else if(result == MemberService.LOGIN_RESULT.BAD_PASSWORD || result == MemberService.LOGIN_RESULT.ID_NOT_FOUND) {
            model.addAttribute("capt_site", CAPTCHA_V3_SITE_KEY);
            model.addAttribute("captcha_version", "v3");
            model.addAttribute("error_visible", "true");
            model.addAttribute("error_text", "다시 시도해주세요");
            model.addAttribute("return", ret);
            return "auth/signin";
        }
        model.addAttribute("capt_site", CAPTCHA_V3_SITE_KEY);
        model.addAttribute("captcha_version", "v3");
        model.addAttribute("error_visible", "true");
        model.addAttribute("error_text", "문제가 발생했습니다. 잠시 후에 다시 시도해주세요.");
        model.addAttribute("return", ret);
        return "auth/signin";
    }
}
