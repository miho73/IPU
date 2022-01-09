package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.exceptions.InvalidInputException;
import com.github.miho73.ipu.library.Converters;
import com.github.miho73.ipu.services.AuthService;
import com.github.miho73.ipu.services.SessionService;
import com.github.miho73.ipu.services.TagService;
import com.github.miho73.ipu.services.UserService;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller("UserControl")
public class UserControl {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired private SessionService sessionService;
    @Autowired private UserService userService;
    @Autowired private AuthService authService;
    @Autowired private TagService tagService;

    @Autowired private Converters converters;

    @GetMapping("/users")
    public String usersPage(Model model, HttpSession session) throws SQLException {
        sessionService.loadSessionToModel(session, model);
        List<User> users = userService.getUserRanking(100);
        model.addAllAttributes(Map.of(
                "users", users,
                "converter", converters
        ));
        return "profile/users.html";
    }

    @GetMapping("/profile")
    public String profileOfYou(@RequestParam(value = "page", required = false, defaultValue = "0") String page,
                               @RequestParam(value = "tab", required = false, defaultValue = "solved") String tab,
                               Model model, HttpSession session) throws SQLException {
        if(!sessionService.checkLogin(session)) {
            return "redirect:/login/?ret=/profile";
        }
        if(!tab.equals("solved") && !tab.equals("stars")) {
            tab = "solved";
        }
        sessionService.loadSessionToModel(session, model);
        String uid = sessionService.getId(session);
        User user = userService.getProfileById(uid);

        if(tab.equals("solved")) {
            int pg = Integer.parseInt(page), PROBLEM_PER_PAGE = 30;
            JSONArray solved = userService.getSolved(pg* PROBLEM_PER_PAGE +1, PROBLEM_PER_PAGE, uid);
            JSONArray processed = tagService.processTagsToHtml(solved, session);
            model.addAllAttributes(Map.of(
                    "pg", pg,
                    "solved", processed.toList(),
                    "hasNext", processed.length() == PROBLEM_PER_PAGE,
                    "hasPrev", pg != 0,
                    "nothing", processed.length() == 0
            ));
        }

        else if(tab.equals("stars")) {
            JSONArray stars = userService.getUserStaredProblem(user.getCode());
            JSONArray processed = tagService.processTagsToHtml(stars, session);
            System.out.println(processed.toList());
            model.addAttribute("stared", processed.toList());
        }

        int currentRank = converters.getLevelCode(user.getExperience());
        String toUp;
        float progressWidth;
        if(currentRank != 8) {
            toUp =  converters.codeTableRl.getOrDefault(currentRank+1, "Unknown")+"까지 "+(converters.cutTable[currentRank]-user.getExperience());
            progressWidth = (float)(user.getExperience()-converters.cutTable[currentRank-1])/(float)(converters.cutTable[currentRank]-converters.cutTable[currentRank-1])*100;
        }
        else {
            toUp = "YOU HAVE THE HIGHEST";
            progressWidth = 100;
        }
        model.addAllAttributes(Map.of(
                "username", user.getName(),
                "userId", user.getId(),
                "bio", user.getBio(),
                "experience", user.getExperience(),
                "currentRank", converters.codeTableRl.getOrDefault(currentRank, "Unset"),
                "lvupInf", toUp,
                "progressBarStyle", converters.codeTable.get(currentRank),
                "progressBarWidth", progressWidth,
                "tab", tab
        ));
        return "profile/userProfile";
    }

    @GetMapping("/profile/{userId}")
    public String profileOfUser(@RequestParam(value = "page", required = false, defaultValue = "0") String page,
                                @PathVariable("userId") String uid,
                                @RequestParam(value = "tab", required = false, defaultValue = "solved") String tab,
                                Model model, HttpSession session) throws SQLException {
        if(!tab.equals("solved") && !tab.equals("stars")) {
            tab = "solved";
        }
        sessionService.loadSessionToModel(session, model);
        User user = userService.getProfileById(uid);

        if(tab.equals("solved")) {
            int pg = Integer.parseInt(page), PROBLEM_PER_PAGE = 30;
            JSONArray solved = userService.getSolved(pg* PROBLEM_PER_PAGE +1, PROBLEM_PER_PAGE, uid);
            JSONArray processed = tagService.processTagsToHtml(solved, session);
            model.addAllAttributes(Map.of(
                "pg", pg,
                "solved", processed.toList(),
                "hasNext", processed.length() == PROBLEM_PER_PAGE,
                "hasPrev", pg != 0,
                "nothing", processed.length() == 0
            ));
        }

        else if(tab.equals("stars")) {
            JSONArray stars = userService.getUserStaredProblem(user.getCode());
            JSONArray processed = tagService.processTagsToHtml(stars, session);
            System.out.println(processed.toList());
            model.addAttribute("stared", processed.toList());
        }

        int currentRank = converters.getLevelCode(user.getExperience());
        String toUp;
        float progressWidth;
        if(currentRank != 8) {
            toUp =  converters.codeTableRl.getOrDefault(currentRank+1, "Unknown")+"까지 "+(converters.cutTable[currentRank]-user.getExperience());
            progressWidth = (float)(user.getExperience()-converters.cutTable[currentRank-1])/(float)(converters.cutTable[currentRank]-converters.cutTable[currentRank-1])*100;
        }
        else {
            toUp = "YOU HAVE THE HIGHEST";
            progressWidth = 100;
        }
        model.addAllAttributes(Map.of(
                "username", user.getName(),
                "userId", user.getId(),
                "bio", user.getBio(),
                "experience", user.getExperience(),
                "currentRank", converters.codeTableRl.getOrDefault(currentRank, "Unset"),
                "lvupInf", toUp,
                "progressBarStyle", converters.codeTable.get(currentRank),
                "progressBarWidth", progressWidth,
                "tab", tab
        ));
        return "profile/userProfile";
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
    public String settingChange(HttpServletRequest request, HttpSession session, HttpServletResponse response,
                                @RequestParam("name") String name, @RequestParam("pwdC") boolean pwdC, @RequestParam("bio") String bio, @RequestParam("npwd") String nPwd, @RequestParam("lpwd") String lPwd) throws Exception {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return null;
        }
        String id=sessionService.getId(session);
        int uCode = sessionService.getCode(session);
        LOGGER.debug("Profile update request: id="+id+", name="+name+", bio="+bio);

        if(!PasswordValidator.matcher(lPwd).matches()) {
            LOGGER.debug("Cannot update profile: legacy password not in format");
            response.setStatus(400);
            return "form-lpwd";
        }
        if(pwdC && !PasswordValidator.matcher(nPwd).matches()) {
            LOGGER.debug("Cannot update profile: new password not in format");
            response.setStatus(400);
            return "form-npwd";
        }

        try {
            if(authService.auth(id, lPwd)) {
                if(bio.length() > 500) {
                    LOGGER.debug("Cannot update profile: profile not in format(bio)");
                    response.setStatus(400);
                    return "form-bio";
                }
                else if(name.length() > 50 || name.equals("")) {
                    LOGGER.debug("Cannot update profile: profile not in format(name)");
                    response.setStatus(400);
                    return "form-name";
                }
                userService.updateProfile(name, bio, uCode);
                sessionService.setName(session, name);
                if(pwdC) {
                    authService.updatePassword(nPwd, uCode);
                }
                return "ok";
            }
            else {
                LOGGER.debug("Profile update password failure");
                response.setStatus(403);
                return "pwd";
            }
        }
        catch (Exception e) {
            LOGGER.error("Cannot update user settings: "+e.getMessage());
            return "error";
        }
    }

    @GetMapping("/settings/bye")
    public String removeAccount(Model model, HttpSession session) {
        if(!sessionService.checkLogin(session)) {
            return "redirect:/login/?ret=/settings/bye";
        }
        sessionService.loadSessionToModel(session, model);
        model.addAllAttributes(Map.of(
                "username", sessionService.getName(session),
                "userid", sessionService.getId(session)
        ));
        return "profile/goodbye";
    }

    @PostMapping("/settings/bye")
    public String byeAcc(Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response, @RequestParam("pwd") String pwd) throws InvalidInputException, SQLException, NoSuchAlgorithmException, IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return null;
        }
        LOGGER.debug("Delete accont. ID="+sessionService.getId(session));
        if(authService.auth(sessionService.getId(session), pwd)) {
            try {
                int uCode = sessionService.getCode(session);
                userService.deleteUesr(uCode);
                sessionService.invalidSession(session);
                return "profile/realbye";
            }
            catch (Exception e) {
                LOGGER.error("Cannot delete user", e);
                sessionService.loadSessionToModel(session, model);
                model.addAllAttributes(Map.of(
                        "username", sessionService.getName(session),
                        "userid", sessionService.getId(session),
                        "fail", "계정을 삭제하지 못했어요. 잠시 후에 다시 시도해주세요."
                ));
                return "profile/goodbye";
            }
        }
        else {
            sessionService.loadSessionToModel(session, model);
            model.addAllAttributes(Map.of(
                    "username", sessionService.getName(session),
                    "userid", sessionService.getId(session),
                    "fail", "잘못된 암호에요."
            ));
            return "profile/goodbye";
        }
    }
}
