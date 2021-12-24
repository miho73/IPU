package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.services.*;
import org.json.JSONObject;
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
import java.sql.SQLException;
import java.util.List;

@Controller("RootControl")
@RequestMapping("/root")
public class RootControl {
    private final InviteService inviteService;
    private final SessionService sessionService;
    private final UserService userService;
    private final ProblemService problemService;
    private final ResourceService resourceService;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public RootControl(InviteService inviteService, SessionService sessionService, UserService userService, ProblemService problemService, ResourceService resourceService) {
        this.inviteService = inviteService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.problemService = problemService;
        this.resourceService = resourceService;
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
                        if(inviteService.inviteCodeValidator(cmdParam[1])) {
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
                        if(inviteService.inviteCodeValidator(cmdParam[2])) {
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
                LOGGER.error("Cannot modify invite code table", e);
                return "SQL Failure: "+e.getMessage();
            }
        }
    }
    @PostMapping("/api/perm")
    @ResponseBody
    public String permissionControl(HttpSession session, HttpServletRequest request, HttpServletResponse response) {
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
                        return "User not exists";
                    }
                    return "Privilege is '"+perm+"'";
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
                    return "OK. Executed query to update privilege.";
                }
                default -> {
                    response.setStatus(400);
                    return "uk";
                }
            }
        } catch (SQLException e) {
            response.setStatus(500);
            LOGGER.error("Cannot update user permission", e);
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
        return "OK Deactivated session";
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
    @GetMapping("/pdb")
    public String pdb(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        sessionService.loadSessionToModel(session, model);
        return "root/pdb";
    }

    @PostMapping("/api/pReq")
    @ResponseBody
    public String sendProblem(HttpSession session, HttpServletResponse response, HttpServletRequest request) throws IOException, SQLException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        int code = Integer.parseInt(request.getParameter("code"));
        Problem p = problemService.getFullProblem(code);
        if(p == null) {
            response.setStatus(404);
            return "PROBLEM_NOT_FOUND";
        }
        JSONObject json = new JSONObject();
        json.put("problem_code", p.getCode());
        json.put("problem_name", p.getName());
        json.put("problem_category", p.getCategory());
        json.put("problem_difficulty", p.getDifficulty());
        json.put("problem_content", p.getContent());
        json.put("problem_solution", p.getSolution());
        json.put("tags", p.getTags());
        json.put("active", p.isActive());
        json.put("author_name", p.getAuthor_name());
        json.put("added_at", p.getAdded_at());
        json.put("last_modified", p.getLast_modified());
        return json.toString();
    }

    @PostMapping("/api/search-resource")
    @ResponseBody
    public String searchResource(HttpSession session, HttpServletResponse response, @RequestParam("code") String code) throws IOException, SQLException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        if(code.equals("LIST")) {
            LOGGER.debug("List all resources");
            return resourceService.getAllResources();
        }
        else if(code.startsWith("code=")) {
            boolean exists = resourceService.isResourceExists(code.substring(5));
            LOGGER.debug("Resource code of "+code.substring(5)+" existence: "+exists);
            if(exists) {
                return resourceService.queryResource(code.substring(5));
            }
            else {
                response.sendError(404);
                return null;
            }
        }
        else if(code.startsWith("name=")) {
            String result = resourceService.queryResourceByName(code.substring(5));
            if(result.equals("")) {
                LOGGER.error("Resource name of "+code.substring(5)+" was not found");
                response.sendError(400);
                return null;
            }
            else return result;
        }
        else {
            LOGGER.error("Resource search query not in format");
            response.sendError(400);
            return null;
        }
    }

    @PostMapping("/api/change-name")
    @ResponseBody
    public String changeName(HttpSession session, HttpServletResponse response, @RequestParam("code") String code, @RequestParam String name) throws IOException, SQLException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        return resourceService.changeName(code, name)?"Successfully changed.":"Cannot change name";
    }

    @PostMapping("/api/problem-using-resource")
    @ResponseBody
    public String searchProblemUsingResource(HttpSession session, HttpServletResponse response, @RequestParam("code") String code) throws SQLException, IOException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        String res = resourceService.searchProblemUsingResource(code);
        if(res.equals("nf")) {
            response.setStatus(400);
            return "{\"error\":\"Resource not found\"}";
        }
        else return res;
    }
}
