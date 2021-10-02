package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.LoginForm;
import com.github.miho73.ipu.domain.SignupForm;
import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.services.InviteService;
import com.github.miho73.ipu.services.SessionService;
import com.github.miho73.ipu.services.AuthService;
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
import java.util.regex.Pattern;

@Controller("AuthControl")
public class AuthControl {
    @Value("${captcha.v3.sitekey}") private String CAPTCHA_V3_SITE_KEY;
    @Value("${captcha.v2.sitekey}") private String CAPTCHA_V2_SITE_KEY;

    private final AuthService userService;
    private final SessionService sessionService;
    private final InviteService inviteService;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthControl.class);

    private final Pattern IdNameValidator = Pattern.compile("^(?=.*[A-Za-z])[A-Za-z0-9]{0,50}$");
    private final Pattern PasswordValidator = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~\\\\\\)]{6,}$");

    @Autowired
    public AuthControl(AuthService userService, SessionService sessionService, InviteService inviteService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.inviteService = inviteService;
    }

    @GetMapping("/login")
    public String getLogin(@RequestParam(value = "ret", required = false, defaultValue = "/") String ret, Model model) {
        model.addAllAttributes(Map.of(
                "capt_site", CAPTCHA_V3_SITE_KEY,
                "captcha_version", "v3",
                "return", ret,
                "error_text", ""
        ));
        return "auth/signin";
    }

    @PostMapping("/login")
    public String postLogin(@RequestParam(value = "ret", required = false, defaultValue = "/") String ret,
                            @ModelAttribute LoginForm loginForm,
                            Model model,
                            HttpSession session) throws IOException {
        if(sessionService.checkLogin(session)) {
            return "redirect:"+ret;
        }
        model.addAllAttributes(Map.of(
                "capt_site", CAPTCHA_V3_SITE_KEY,
                "captcha_version", "v3",
                "return", ret
        ));

        // Login form validator
        if(!IdNameValidator.matcher(loginForm.getId()).matches() || !PasswordValidator.matcher(loginForm.getPassword()).matches()) {
            LOGGER.debug("Invalid login form: id="+loginForm.getId());
            model.addAttribute("error_text", "ID 또는 암호가 형식에 맞지 않습니다.");
            return "auth/signin";
        }

        AuthService.LOGIN_RESULT result;
        try {
            result = userService.checkLogin(loginForm, session);
        } catch (SQLException | InvalidInputException | NoSuchAlgorithmException | IllegalArgumentException e) {
            LOGGER.error("Login error. Form: id="+loginForm.getId()+", gVers="+loginForm.getgVers()+", gToken="+loginForm.getgToken());
            LOGGER.error(e.getMessage(), e);
            model.addAttribute("error_text", "문제가 발생했습니다. 잠시 후에 다시 시도해주세요.");
            return "auth/signin";
        }

        if(result == AuthService.LOGIN_RESULT.OK) {
            return "redirect:"+ret;
        }
        else if(result == AuthService.LOGIN_RESULT.CAPTCHA_FAILED) {
            model.addAllAttributes(Map.of(
                    "capt_site", CAPTCHA_V2_SITE_KEY,
                    "captcha_version", "v2",
                    "error_text", "CAPTCHA 인증에 실패했습니다. 다시 시도해주세요."
            ));
            return "auth/signin";
        }
        else if (result == AuthService.LOGIN_RESULT.BLOCKED) {
            model.addAttribute("error_text", "이 계정으로는 로그인할 수 없습니다.");
            return "auth/signin";
        }
        else if(result == AuthService.LOGIN_RESULT.BAD_PASSWORD || result == AuthService.LOGIN_RESULT.ID_NOT_FOUND) {
            model.addAttribute("error_text", "다시 시도해주세요.");
            return "auth/signin";
        }
        model.addAttribute("error_text", "문제가 발생했습니다. 잠시 후에 다시 시도해주세요.");
        return "auth/signin";
    }

    @GetMapping("/login/deauth")
    public String invalidSession(HttpSession session) {
        sessionService.invalidSession(session);
        LOGGER.debug("Invalidated session "+session.getId());
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String getSignup(Model model) {
        model.addAllAttributes(Map.of(
                "capt_site", CAPTCHA_V2_SITE_KEY,
                "error_text", ""
        ));
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupForm form,
                         Model model,
                         HttpSession session) throws SQLException, InvalidInputException, NoSuchAlgorithmException, IOException {
        boolean wasError = false;
        StringBuilder errTxt = new StringBuilder();

        model.addAttribute("capt_site", CAPTCHA_V2_SITE_KEY);
        if(!IdNameValidator.matcher(form.getId()).matches()) {
            errTxt.append("ID는 50자 이내의 알파벳이나 숫자여야 합니다.<br/>");
            LOGGER.debug("Signup id regex failure: "+form.getId());
            wasError = true;
        }
        if(!IdNameValidator.matcher(form.getId()).matches()) {
            errTxt.append("이름은 50자 이내의 알파벳이나 숫자여야 합니다.<br/>");
            LOGGER.debug("Signup name regex failure: "+form.getName());
            wasError = true;
        }
        if(!PasswordValidator.matcher(form.getPassword()).matches()) {
            errTxt.append("암호는 6글자 이상에 영어, 숫자 한 글자 이상을 가져야 합니다.<br/>");
            LOGGER.debug("Signup password regex failure");
            wasError = true;
        }
        if(wasError) {
            model.addAttribute("error_text", errTxt.toString());
            return "auth/signup";
        }

        AuthService.SIGNUP_RESULT result = userService.addUser(new User(form.getId(), form.getName(), form.getPassword(), form.getInvite()), form.getgToken());
        if(result == AuthService.SIGNUP_RESULT.CAPTCHA_FAILED) {
            model.addAttribute("error_text", "CAPTCHA 인증에 실패했습니다. 다시 시도해주세요.");
            return "auth/signup";
        }
        if(result == AuthService.SIGNUP_RESULT.INVALID_INVITE) {
            model.addAttribute("error_text", "유효하지 않은 초대코드입니다. 다시 시도해주세요.");
            return "auth/signup";
        }
        if(result == AuthService.SIGNUP_RESULT.DUPLICATED_ID) {
            model.addAttribute("error_text", "이미 사용중인 ID 입니다. 다른 ID를 골라주세요.");
            return "auth/signup";
        }
        return "redirect:/";
    }

    @PostMapping("/api/invite-check")
    @ResponseBody
    public String inviteCheck(@RequestBody String code) throws SQLException {
        boolean ok = false;
        if(code.length()>=4){
            ok = inviteService.checkExists(code.substring(code.length() - 4));
        }
        LOGGER.debug("Invite code challenge. "+code+": "+(ok?"succeed":"failed"));
        return Boolean.toString(ok);
    }
}
