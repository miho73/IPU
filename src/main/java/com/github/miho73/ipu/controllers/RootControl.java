package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.services.InviteService;
import com.github.miho73.ipu.services.SessionService;
import com.github.miho73.ipu.services.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.plaf.PanelUI;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Controller("RootControl")
@RequestMapping("/root")
public class RootControl {
    private final InviteService inviteService;
    private final SessionService sessionService;
    private final UserService userService;

    @Autowired
    public RootControl(InviteService inviteService, SessionService sessionService, UserService userService) {
        this.inviteService = inviteService;
        this.sessionService = sessionService;
        this.userService = userService;
    }

    @GetMapping("")
    public String manage(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        if(
                sessionService.hasPrivilege(SessionService.PRIVILEGES.INVITE_CODES, session) &&
                sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)
        ) {
            response.sendError(404);
            return null;
        }
        sessionService.loadSessionToModel(session, model);
        return "root/control";
    }

    @PostMapping(value = "/api/inv", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String inviteControl(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.INVITE_CODES, session)) {
            response.sendError(403);
            return null;
        }
        String cmd = request.getParameter("q");
        String[] cmdParam = cmd.split(" ");
        if(cmdParam.length < 1 || cmdParam.length>3) {
            response.setStatus(400);
            return "Command is too short or long";
        }
        else {
            try {
                switch (cmdParam[0]) {
                    case "APPEND" -> {
                        if (cmdParam.length < 2) {
                            response.setStatus(400);
                            return "Argument is not enough to execute APPEND";
                        }
                        if(!inviteService.inviteCodeValidator(cmdParam[1])) {
                            response.setStatus(400);
                            return "Invalid Invite code format";
                        }
                        inviteService.IDU(cmdParam, InviteService.DB_ACTION.INSERT);
                    }
                    case "UPDATE" -> {
                        if (cmdParam.length < 3) {
                            response.setStatus(400);
                            return "Argument is not enough to execute UPDATE";
                        }
                        if(!inviteService.inviteCodeValidator(cmdParam[2])) {
                            response.setStatus(400);
                            return "Invalid Invite code format";
                        }
                        inviteService.IDU(cmdParam, InviteService.DB_ACTION.UPDATE);
                    }
                    case "DELETE" -> {
                        if (cmdParam.length < 2) {
                            response.setStatus(400);
                            return "Argument is not enough to execute DELETE";
                        }
                        inviteService.IDU(cmdParam, InviteService.DB_ACTION.DELETE);
                    }
                    case "GET" -> {}
                    default -> {
                        response.setStatus(400);
                        return "Unknown command";
                    }
                }
                List<String> codes = inviteService.listCodes();
                JSONObject obj = new JSONObject();
                obj.put("codes", codes);
                return obj.toString();
            }
            catch (SQLException e) {
                response.setStatus(500);
                return "SQL Failure: "+e.getMessage();
            }
        }
    }
    @PostMapping("/api/perm")
    @ResponseBody
    public String permissionControl(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.SUPERUSER, session)) {
            response.setStatus(403);
            return "perm";
        }
        String cmd = request.getParameter("q");
        String[] cmdParam = cmd.split(" ");
        if(cmdParam.length < 1 || cmdParam.length>3) {
            response.setStatus(400);
            return "uk";
        }
        try {
            switch (cmdParam[0]) {
                case "QUERY" -> {
                    if(cmdParam.length < 2) {
                        response.setStatus(400);
                        return "unc";
                    }
                    String perm = (String) userService.getUserDataById(cmdParam[1], "privilege");
                    if(perm == null) {
                        return "User \'"+cmdParam[2]+"\' is not exists";
                    }
                    return "Privilege of \'"+cmdParam[1]+"\' is \'"+perm+"\'";
                }
                case "UPDATE" -> {
                    if(cmdParam.length < 3) {
                        response.setStatus(400);
                        return "unc";
                    }
                    if(sessionService.getId(session).equals(cmdParam[2])) {
                        response.setStatus(400);
                        return "self";
                    }
                    if(cmdParam[1].equals("") || cmdParam[1].length() > 5) {
                        response.setStatus(400);
                        return "permFormat";
                    }
                    userService.updateStringById(cmdParam[2], "privilege", cmdParam[1]);
                    HttpSession sess = sessionService.getSessonById(cmdParam[2]);
                    if(sess != null) {
                        sess.setAttribute("privilege", cmdParam[1]);
                    }
                    return "OK. Executed query to update privilege of user \'"+cmdParam[2]+"\' to \'"+cmdParam[1]+"\'.";
                }
                default -> {
                    response.setStatus(400);
                    return "uk";
                }
            }
        } catch (SQLException e) {
            response.setStatus(500);
            return "dbquery";
        }
    }
    @PostMapping("/api/deauth")
    @ResponseBody
    public String deAuth(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.SUPERUSER, session)) {
            response.setStatus(403);
            return "perm";
        }
        String id = request.getParameter("id");
        HttpSession sess = sessionService.getSessonById(id);
        if(sess == null) {
            response.setStatus(400);
            return "usr";
        }
        sessionService.invalidSession(sess);
        return "OK Deactivated session of \'"+id+"\'";
    }

    @GetMapping("/resources")
    public String resource(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        sessionService.loadSessionToModel(session, model);
        return "root/resources";
    }
}
