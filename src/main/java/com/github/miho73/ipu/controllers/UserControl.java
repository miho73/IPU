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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        int uCode = sessionService.getCode(session);
        String uid = sessionService.getId(session);
        User user = userService.getProfileById(uid);

        // Solved tab
        if(tab.equals("solved")) {
            int pg = Integer.parseInt(page), PROBLEM_PER_PAGE = 30;
            JSONArray solved = userService.getSolved(pg* PROBLEM_PER_PAGE, PROBLEM_PER_PAGE, uCode);
            JSONArray processed = tagService.processTagsToHtml(solved, session);
            model.addAllAttributes(Map.of(
                    "pg", pg,
                    "solved", processed.toList(),
                    "hasNext", processed.length() == PROBLEM_PER_PAGE,
                    "hasPrev", pg != 0,
                    "nothing", processed.length() == 0
            ));
        }

        // Stars tab
        else {
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
        sessionService.loadSessionToModel(session, model);
        User user = userService.getProfileById(uid);

        if(tab.equals("solved")) {
            int pg = Integer.parseInt(page), PROBLEM_PER_PAGE = 30;
            int uCode = (int) userService.getUserDataById(uid, "user_code");
            JSONArray solved = userService.getSolved(pg* PROBLEM_PER_PAGE +1, PROBLEM_PER_PAGE, uCode);
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

        else {
            return "redirect:/profile/"+uid;
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
    public void settingsDirect(HttpServletResponse response) throws IOException {
        response.sendRedirect("/settings/personal");
    }

    @GetMapping("/settings/{tab}")
    public String settings(Model model, HttpSession session, @PathVariable(name = "tab") String tab,
                           @RequestParam(name = "task", required = false, defaultValue = "") String task,
                           @RequestParam(name = "success", required = false, defaultValue = "false") boolean success) throws SQLException {
        if(!sessionService.checkLogin(session)) {
            return "redirect:/login/?ret=/settings";
        }
        sessionService.loadSessionToModel(session, model);
        User user = userService.getUserByCode(sessionService.getCode(session));
        model.addAllAttributes(Map.of(
                "username", user.getName(),
                "userId", user.getId(),
                "bio", user.getBio(),
                "task", task,
                "success", success
        ));
        switch (tab) {
            case "personal" -> model.addAllAttributes(Map.of(
                    "email", user.getEmail() == null ? "" : user.getEmail(),
                    "tab", 0
            ));
            case "account" -> model.addAttribute("tab", 1);
            case "security" -> model.addAttribute("tab", 2);
            case "info" -> {
                Timestamp ls = user.getLastSolve();
                String lss;
                if (ls == null) lss = "N/A";
                else lss = ls.toString();
                model.addAllAttributes(Map.of(
                        "joined", user.getJoined().toString(),
                        "lastLogin", user.getLastLogin().toString(),
                        "lastSolve", lss,
                        "usrCd", user.getCode(),
                        "tab", 3
                ));
            }
            default -> model.addAttribute("tab", -1);
        }
        return "profile/settings";
    }

    private final Pattern IdNameValidator = Pattern.compile("^(?=.*[A-Za-z])[A-Za-z0-9]{0,50}$");
    private final Pattern PasswordValidator = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~\\\\\\)]{6,}$");

    @PostMapping("/settings/api/update/profile")
    public void updateProfile(HttpSession session, HttpServletResponse response,
                              @RequestParam("name") String name, @RequestParam("bio") String bio) throws IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return;
        }
        String id = sessionService.getId(session);
        try {
            userService.updateProfile(name, bio, sessionService.getCode(session));
            sessionService.setName(session, name);
            LOGGER.debug("Updated profile for user "+id);
            response.sendRedirect("/settings/personal/?task=profile&success=true");
        } catch (SQLException e) {
            LOGGER.error("Cannot update profile for user "+id, e);
            response.sendRedirect("/settings/personal/?task=profile&success=false");
        }
    }

    @PostMapping("/settings/api/update/email")
    public void updateEmail(HttpSession session, HttpServletResponse response,
                            @RequestParam("email") String email) throws IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return;
        }
        response.sendRedirect("/settings/personal/?task=email&success=false");
    }

    @GetMapping("/settings/request/password/update")
    public void requestPasswordUpdate(HttpSession session, HttpServletResponse response) throws IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return;
        }
        sessionService.prepareSudo(session, "password", "/settings/confirm/password/update");
        response.sendRedirect("/sudo");
    }
    @GetMapping("/settings/confirm/password/update")
    public String confirmPasswordUpdate(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return null;
        }
        if(sessionService.getAttribute(session, "sudoResult") == null) {
            response.sendError(403);
            return null;
        }
        if(!sessionService.getAttribute(session, "sudoResult").equals("password")) {
            response.sendError(403);
            return null;
        }
        return "auth/updatePwd";
    }
    @PostMapping("/settings/confirm/password/update")
    public void completePasswordUpdate(HttpSession session, HttpServletResponse response,
                                         @RequestParam("pwd") String pwd,
                                         @RequestParam("pwdc") String pwdc) throws IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return;
        }
        if(sessionService.getAttribute(session, "sudoResult") == null) {
            LOGGER.warn("password change denied: sudo token not found");
            response.sendRedirect("/settings/security/?task=upwd&success=false");
            return;
        }
        if(!sessionService.getAttribute(session, "sudoResult").equals("password")) {
            LOGGER.warn("password change denied: invalid sudo token");
            response.sendRedirect("/settings/security/?task=upwd&success=false");
            return;
        }
        if(!pwd.equals(pwdc)) {
            response.sendRedirect("/settings/security/?task=upwd&success=false");
        }
        try {
            authService.updatePassword(pwd, sessionService.getCode(session));
            LOGGER.warn("changed password for user "+sessionService.getId(session));
            response.sendRedirect("/settings/security/?task=upwd&success=true");
        } catch (NoSuchAlgorithmException | InvalidInputException | SQLException | RuntimeException e) {
            LOGGER.error("cannot change password for user "+sessionService.getId(session), e);
            response.sendRedirect("/settings/security/?task=upwd&success=false");
        } finally {
            sessionService.completeSudo(session);
        }
    }

    @GetMapping("/settings/request/account/reset")
    public void requestAccountReset(HttpSession session, HttpServletResponse response) throws IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return;
        }
        sessionService.prepareSudo(session, "reset", "/settings/confirm/account/reset");
        response.sendRedirect("/sudo");
    }
    @GetMapping("/settings/confirm/account/reset")
    public void confirmAccountReset(HttpSession session, HttpServletResponse response) throws IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return;
        }
        if(sessionService.getAttribute(session, "sudoResult") == null) {
            LOGGER.warn("account reset denied: sudo token not found");
            response.sendRedirect("/settings/account/?task=reset&success=false");
            return;
        }
        if(!sessionService.getAttribute(session, "sudoResult").equals("reset")) {
            LOGGER.warn("account reset denied: invalid sudo token");
            response.sendRedirect("/settings/account/?task=reset&success=false");
            return;
        }
        try {
            userService.resetAccount(sessionService.getCode(session));
            response.sendRedirect("/settings/account/?task=reset&success=true");
            LOGGER.warn("account reset: "+sessionService.getId(session));
        } catch (SQLException e) {
            LOGGER.error("cannot reset account: "+sessionService.getId(session), e);
            response.sendRedirect("/settings/account/?task=reset&success=false");
        } finally {
            sessionService.completeSudo(session);
        }
    }

    @GetMapping("/settings/request/account/delete")
    public String removeAccount(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return null;
        }
        sessionService.loadSessionToModel(session, model);
        model.addAllAttributes(Map.of(
                "username", sessionService.getName(session),
                "userid", sessionService.getId(session)
        ));
        authService.modelCaptchaV2(model);
        return "profile/goodbye";
    }

    @PostMapping("/settings/request/account/delete")
    public String byeAcc(Model model, HttpSession session, HttpServletRequest request, HttpServletResponse response,
                         @RequestParam("pwd") String pwd,
                         @RequestParam("gToken") String gToken,
                         @RequestParam("gVers") String gVers) throws InvalidInputException, SQLException, NoSuchAlgorithmException, IOException {
        if(!sessionService.checkLogin(session)) {
            response.sendError(403);
            return null;
        }
        LOGGER.debug("Delete accont. ID="+sessionService.getId(session));
        int auth = authService.auth(sessionService.getId(session), pwd, gToken, gVers, request);
        if(auth == 0) {
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
                authService.modelCaptchaV2(model);
                return "profile/goodbye";
            }
        }
        else {
            sessionService.loadSessionToModel(session, model);
            model.addAllAttributes(Map.of(
                    "username", sessionService.getName(session),
                    "userid", sessionService.getId(session)
            ));
            authService.modelCaptchaV2(model);
            switch (auth) {
                case 1 -> model.addAttribute("fail", "인증할 수 없어요");
                case 2 -> model.addAttribute("fail", "인증 대상을 찾을 수 없어요");
                case 3 -> model.addAttribute("fail", "암호가 잘못되었어요");
                case 4 -> model.addAttribute("fail", "CAPTCHA 확인에 실패했어요");
            }
            return "profile/goodbye";
        }
    }
}
