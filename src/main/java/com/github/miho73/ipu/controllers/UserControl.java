package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.repositories.UserRepository;
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
import java.sql.SQLException;
import java.util.Map;

@Controller("UserControl")
public class UserControl {
    private final SessionService sessionService;
    private final UserService userService;

    @Autowired
    public UserControl(SessionService sessionService, UserService userService) {
        this.sessionService = sessionService;
        this.userService = userService;
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
}
