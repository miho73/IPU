package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.LoginForm;
import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.repositories.UserRepository;
import com.github.miho73.ipu.services.AuthService;
import com.github.miho73.ipu.services.SessionService;
import com.github.miho73.ipu.services.UserService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Controller("UserControl")
public class UserControl {
    private final SessionService sessionService;
    private final UserService userService;
    private final AuthService authService;

    @Autowired
    public UserControl(SessionService sessionService, UserService userService, AuthService authService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping("/users")
    public String usersPage(Model model, HttpSession session) {
        sessionService.loadSessionToModel(session, model);
        return "profile/users.html";
    }

    @PostMapping(value = "/api/users", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String userRanking(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        long len = Long.parseLong(request.getParameter("len"));
        if(len<=0 || len>100) {
            response.sendError(400);
            return null;
        }
        return userService.getUserRanking(len);
    }

    @GetMapping("/profile")
    public String profileOfYou(@RequestParam(value = "page", required = false, defaultValue = "0") String page, Model model, HttpSession session) throws SQLException {
        if(!sessionService.checkLogin(session)) {
            return "redirect:/login/?ret=/profile";
        }
        sessionService.loadSessionToModel(session, model);
        String uid = sessionService.getId(session);
        User user = userService.getProfileById(uid);
        model.addAllAttributes(Map.of(
                "username", user.getName(),
                "userId", user.getId(),
                "bio", user.getBio(),
                "experience", user.getExperience(),
                "pg", Integer.parseInt(page)
        ));
        return "profile/userProfile";
    }

    @GetMapping("/profile/{userId}")
    public String profileOfUser(@RequestParam(value = "page", required = false, defaultValue = "0") String page, @PathVariable("userId") String uid, Model model, HttpSession session) throws SQLException {
        sessionService.loadSessionToModel(session, model);
        User user = userService.getProfileById(uid);
        model.addAllAttributes(Map.of(
                "username", user.getName(),
                "userId", user.getId(),
                "bio", user.getBio(),
                "experience", user.getExperience(),
                "pg", Integer.parseInt(page)
        ));
        return "profile/userProfile";
    }

    @PostMapping(value = "/api/get-solved", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getSolved(HttpServletRequest request) throws SQLException {
        long frm = Long.parseLong(request.getParameter("frm"));
        long len = Long.parseLong(request.getParameter("len"));
        String id = request.getParameter("id");
        return userService.getSolved(frm, len, id);
    }

    @GetMapping("/settings")
    public String settings(Model model, HttpSession session) throws SQLException {
        if(!sessionService.checkLogin(session)) {
            return "redirect:/login/?ret=/settings";
        }
        sessionService.loadSessionToModel(session, model);
        User user = userService.getUserByCode(sessionService.getCode(session));
        Timestamp ls = user.getLastSolve();
        String lss;
        if(ls == null) lss="N/A";
        else lss = ls.toString();
        model.addAllAttributes(Map.of(
                "username", user.getName(),
                "userId", user.getId(),
                "bio", user.getBio(),
                //"email", user.getEmail(),
                "joined", user.getJoined().toString(),
                "lastLogin", user.getLastLogin().toString(),
                "lastSolve", lss,
                "usrCd", user.getCode()
        ));
        return "profile/settings";
    }

    private final Pattern IdNameValidator = Pattern.compile("^(?=.*[A-Za-z])[A-Za-z0-9]{0,50}$");
    private final Pattern PasswordValidator = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~\\\\\\)]{6,}$");

    @PostMapping("/settings")
    @ResponseBody
    public String settingChange(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException, InvalidInputException, SQLException, NoSuchAlgorithmException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return null;
        }
        String id=sessionService.getId(session), name = request.getParameter("name");
        String bio = request.getParameter("bio");
        boolean pwdC = Boolean.parseBoolean(request.getParameter("pwdC"));
        String nPwd = request.getParameter("npwd"), lPwd = request.getParameter("lpwd");
        long uCode = sessionService.getCode(session);

        if(!PasswordValidator.matcher(lPwd).matches()) {
            response.setStatus(400);
            return "form";
        }

        if(authService.auth(id, lPwd)) {
            if(bio.length() > 500 || name.length() > 50 || name.equals("")) {
                response.setStatus(400);
                return "form";
            }
            userService.updateProfile(name, bio, uCode);
            sessionService.setName(session, name);
            if(pwdC) {
                if(!PasswordValidator.matcher(nPwd).matches()) {
                    response.setStatus(400);
                    return "fpwd";
                }
                authService.updatePassword(nPwd, uCode);
            }
            return "ok";
        }
        else {
            response.setStatus(403);
            return "pwd";
        }
    }

    @GetMapping("/settings/bye")
    public String removeAccount(Model model, HttpSession session) {
        if(!sessionService.checkLogin(session)) {
            return "redirect:/login/?ret=/settings/bye";
        }
        model.addAllAttributes(Map.of(
                "username", sessionService.getName(session),
                "userid", sessionService.getId(session)
        ));
        return "profile/goodbye";
    }

    @PostMapping("/settings/bye")
    //TODO: use transaction
    public String byeAcc(Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response) throws InvalidInputException, SQLException, NoSuchAlgorithmException, IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return null;
        }
        String pwd = request.getParameter("pwd");
        if(authService.auth(sessionService.getId(session), pwd)) {
            long uCode = sessionService.getCode(session);
            userService.deleteUesr(uCode);
            sessionService.invalidSession(session);
            return "profile/realbye";
        }
        else {
            model.addAllAttributes(Map.of(
                    "username", sessionService.getName(session),
                    "userid", sessionService.getId(session),
                    "fail", "올바르지 않은 암호입니다"
            ));
            return "profile/goodbye";
        }
    }
}
