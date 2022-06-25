package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.library.exceptions.CaptchaFailureException;
import com.github.miho73.ipu.library.exceptions.DuplicatedException;
import com.github.miho73.ipu.library.exceptions.ForbiddenException;
import com.github.miho73.ipu.library.rest.response.RestfulReponse;
import com.github.miho73.ipu.services.AuthService;
import com.github.miho73.ipu.services.InviteService;
import com.github.miho73.ipu.services.SessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Pattern;

@Controller("AuthControl")
public class AuthControl {
    @Autowired private AuthService authService;
    @Autowired private SessionService sessionService;
    @Autowired private InviteService inviteService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private final Pattern IdValidator = Pattern.compile("^(?=.*[A-Za-z])[A-Za-z0-9]{0,50}$");
    private final Pattern PasswordValidator = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~\\\\\\)]{6,}$");

    // Get login page
    @GetMapping("/login")
    public String getLogin(Model model, HttpServletResponse response, HttpSession session,
                           @RequestParam(value = "ret", required = false, defaultValue = "/") String ret,
                           @RequestParam(value = "error", required = false, defaultValue = "0") int error,
                           @RequestParam(value = "captcha", required = false, defaultValue = "3") int captchaVersion) throws IOException {

        // redirect if user is already logged in
        if(sessionService.checkLogin(session)) {
            return "redirect:"+ret;
        }

        // Send Bad Request if return address is too long
        if(ret.length() >= 100) {
            response.sendError(400);
            return null;
        }
        model.addAllAttributes(Map.of(
                "return", ret,
                "error", error
        ));
        // load captcha
        if(captchaVersion == 3) authService.modelCaptchaV3(model);
        else if(captchaVersion == 2) authService.modelCaptchaV2(model);
        else {
            response.sendError(400);
            return null;
        }
        return "auth/signin";
    }

    /**
     * Process login request
     * Error cods:
     * 1 is id or password does not satisfies form
     */
    @PostMapping("/login")
    public String  postLogin(Model model, HttpSession session, HttpServletRequest request,
                             @RequestParam(value = "ret", required = false, defaultValue = "/") String ret,
                             @RequestParam("id") String id,
                             @RequestParam("password") String password,
                             @RequestParam("gToken") String gToken,
                             @RequestParam("gVers") String gVers) {

        if(sessionService.checkLogin(session)) {
            return "redirect:"+ret;
        }
        model.addAttribute("return", ret);
        authService.modelCaptchaV3(model);

        // Login form validator
        if(!IdValidator.matcher(id).matches() || !PasswordValidator.matcher(password).matches()) {
            return "redirect:/login/?error=1&ret="+ret;
        }

        try {
            int result = authService.completeLogin(id, password, gToken, gVers, session, request);

            if(result == 0) { // login success
                if(!ret.startsWith("/")) return "redirect:/";
                return "redirect:"+ret;
            }
            else if(result == 1) { // login locked
                return "redirect:/login/?error=2&ret="+ret;
            }
            else if(result == 2 || result == 3) { // id/password failure
                return "redirect:/login/?error=3&ret="+ret;
            }
            else if (result == 4) { // user blocked
                return "redirect:/login/?error=4&ret="+ret;
            }
            else if(result == 5) { // CAPTCHA failure
                return "redirect:/login/?error=5&captcha=2&ret="+ret;
            }
            else { // none of above
                return "redirect:/login/?error=6&ret="+ret;
            }
        } catch (Exception e) {
            return "redirect:/login/?error=6&ret="+ret;
        }
    }

    // sign out
    @GetMapping("/login/deauth")
    public String invalidSession(HttpSession session) {
        if(session == null || !sessionService.checkLogin(session)) {
            return "redirect:/";
        }
        sessionService.clearSudo(session);
        sessionService.completeSudo(session);
        sessionService.invalidSession(session);
        LOGGER.debug("Invalidated session "+session.getId());
        return "redirect:/";
    }

    // Get sign up page
    @GetMapping("/signup")
    public String getSignup(Model model) {
        authService.modelCaptchaV2(model);
        return "auth/signup";
    }

    // Create user account
    @PostMapping(value = "/api/account/create", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String signup(HttpServletResponse response, HttpSession session,
                         @RequestParam("id") String id,
                         @RequestParam("password") String pwd,
                         @RequestParam("name") String name,
                         @RequestParam("invite") String invite,
                         @RequestParam("gToken") String gToken) {

        // check form
        if(!IdValidator.matcher(id).matches()) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "badform");
        }
        if(name.length() > 50) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "badform");
        }
        if(!PasswordValidator.matcher(pwd).matches()) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "badform");
        }

        try {
            authService.addUser(new User(id, name, pwd, invite), gToken);
            return RestfulReponse.createRestfulResponse(HttpStatus.OK);
        } catch (IOException | SQLException | InvalidInputException | NoSuchAlgorithmException e) {
            response.setStatus(500);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CaptchaFailureException e) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "captcha");
        } catch (ForbiddenException e) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "invite");
        } catch (DuplicatedException e) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "id_dupl");
        }
    }

    @GetMapping("/sudo")
    public String authSudo(Model model, HttpSession session, HttpServletResponse response) {
        if(!sessionService.checkLogin(session)) {
            model.addAllAttributes(Map.of(
                    "blocked", true,
                    "reason", "로그인되어있지 않아요",
                    "fail", false
            ));
            return "auth/sudo";
        }
        sendSudo(model, session);
        return "auth/sudo";
    }
    @PostMapping("/sudo")
    public String sudoViaSudo(Model model, HttpSession session, HttpServletResponse response, HttpServletRequest request,
                              @RequestParam("pwd") String pwd,
                              @RequestParam("gToken") String gToken,
                              @RequestParam("gVers") String gVers) throws IOException {
        if(!sessionService.checkLogin(session)) {
            model.addAllAttributes(Map.of(
                    "blocked", true,
                    "reason", "로그인되어있지 않아요",
                    "fail", false
            ));
            return "auth/sudo";
        }
        try {
            if(!PasswordValidator.matcher(pwd).matches()) {
                sendSudo(model, session, "암호의 형식이 알맞지 않아요");
                return "auth/sudo";
            }
            int auth = authService.auth(sessionService.getId(session), pwd, gToken, gVers, request);
            if(auth == 0) {
                String ret = (String) sessionService.getAttribute(session, "sudoRet");
                sessionService.setAttribute(session, "sudoResult", session.getAttribute("sudo"));
                sessionService.clearSudo(session);
                response.sendRedirect(ret);
                return null;
            }
            else {
                switch (auth) {
                    case 1 -> sendSudo(model, session, "인증할 수 없어요");
                    case 2 -> sendSudo(model, session, "인증대상을 찾을 수 없어요");
                    case 3 -> sendSudo(model, session, "암호가 잘못되었어요");
                    case 4 -> sendSudo(model, session, "CAPTCHA 확인에 실패했어요");
                }
                return "auth/sudo";
            }
        } catch (SQLException | InvalidInputException | NoSuchAlgorithmException e) {
            sendSudo(model, session, "문제가 생겨서 인증하지 못했어요");
            return "auth/sudo";
        }
    }
    private void sendSudo(Model model, HttpSession session, String... error) {
        // clear old request
        if(sessionService.getAttribute(session, "sudoTime") != null) {
            if(Instant.now().getEpochSecond() - (long)session.getAttribute("sudoTime") >= 180) {
                sessionService.clearSudo(session);
            }
        }

        String sudo = (String) sessionService.getAttribute(session, "sudo");
        if(sudo == null) {
            model.addAllAttributes(Map.of(
                    "blocked", true,
                    "reason", "유효한 인증이 없습니다.",
                    "fail", false
            ));
            return;
        }
        model.addAttribute("blocked", false);
        authService.modelCaptchaV3(model);
        if(error.length != 0) {
            model.addAttribute("error", error[0]);
            model.addAttribute("fail", true);
        }
        else {
            model.addAttribute("fail", false);
        }
        switch (sudo) {
            case "reset" -> model.addAttribute("purpose", "계정이 초기화");
            case "delete" -> model.addAttribute("purpose", "계정이 삭제");
            case "password" -> model.addAttribute("purpose", "암호를 변경하게");
            default -> model.addAttribute("purpose", null);
        }
    }

    @PostMapping(value = "/api/invite/check", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String inviteCheck(@RequestBody String code) {
        boolean ok = false;
        try {
            if(code.length()>=4){
                ok = inviteService.checkExists(code.substring(code.length() - 4));
            }
            LOGGER.debug("Invite code challenge. "+code+": "+ok);
            return RestfulReponse.createRestfulResponse(HttpStatus.OK, ok);
        }
        catch (SQLException e) {
            LOGGER.error("Cannoot check invite code: "+code, e);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
