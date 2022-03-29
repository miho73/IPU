package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.Issue;
import com.github.miho73.ipu.library.exceptions.ForbiddenException;
import com.github.miho73.ipu.library.exceptions.ResourceNotFoundException;
import com.github.miho73.ipu.library.ipuac.Renderer;
import com.github.miho73.ipu.library.rest.response.RestfulReponse;
import com.github.miho73.ipu.services.IssueService;
import com.github.miho73.ipu.services.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Controller("IssueController")
@RequestMapping("issue")
public class IssueController {
    @Autowired SessionService sessionService;
    @Autowired IssueService issueService;
    @Autowired ProblemControl problemControl;
    @Autowired Renderer renderer;

    private final int ISSUE_PER_PAGE = 30;
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");

    public IssueController() {
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @GetMapping("")
    public String getIssuePage(@RequestParam(name = "page", required = false, defaultValue = "0") int page,
                               Model model, HttpSession session) throws SQLException {
        sessionService.loadSessionToModel(session, model);
        List<Issue> issueList = issueService.getIssueList(page*ISSUE_PER_PAGE+1, ISSUE_PER_PAGE);
        model.addAllAttributes(Map.of(
                "issue", issueList
        ));
        return "issue/issuePage";
    }

    @GetMapping("/{issueCode}")
    public String getIssueViewerPage(@PathVariable("issueCode") int issueCode,
                                     Model model, HttpSession session) throws SQLException {
        sessionService.loadSessionToModel(session, model);

        Issue issue = issueService.getIssue(issueCode);
        model.addAllAttributes(Map.of(
                "issueCode", issue.getIssueCode(),
                "issueName", issue.getIssueName(),
                "issueWritten", issue.getOpenAt(),
                "issueAuthor", issue.getAuthor(),
                "issueContent", renderer.IPUACtoHTML(issue.getContent()),
                "isYours", issue.getAuthor().equals(sessionService.getId(session))
        ));
        return "issue/issueViewPage";
    }

    @GetMapping("/new")
    public String createNewIssuePage(Model model, HttpSession session) {
        if(!sessionService.checkLogin(session)) {
            return "redirect:/login/?ret=/issue/new";
        }
        sessionService.loadSessionToModel(session, model);
        return "issue/createNewIssue";
    }

    @GetMapping("/api/get/{param}")
    @ResponseBody
    public String getIssue(@RequestParam("issue-code") int code,
                           @PathVariable("param") String column,
                           HttpServletResponse response) {
        try {
            Issue issue = issueService.getIssue(code);
            if(issue == null) {
                response.setStatus(404);
                return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.INTERNAL_SERVER_ERROR, "issue code with "+code+" was not found");
            }
            String result;
            switch (column) {
                case "name" -> result = issue.getIssueName();
                case "content" -> result = issue.getContent();
                case "author" -> result = issue.getAuthor();
                case "open-at" -> result = df.format(issue.getOpenAt());
                case "status" -> result = issue.getStatus().toString();
                case "for-problem" -> result = Integer.toString(issue.getForProblem());
                case "vote" -> result = Integer.toString(issue.getVote());
                default -> {
                    response.setStatus(400);
                    result = RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.INTERNAL_SERVER_ERROR, "unknown issue element");
                }
            }
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.OK, result);
        }
        catch (SQLException e) {
            response.setStatus(500);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.INTERNAL_SERVER_ERROR, "database error");
        }
    }

    @PostMapping("/api/create-new")
    @ResponseBody
    public String newIssue(@RequestParam("name") String name,
                           @RequestParam("type") int type,
                           @RequestParam("pCode") int pCode,
                           @RequestParam("content") String content,
                           HttpSession session, HttpServletResponse response) {
        if(!sessionService.checkLogin(session)) {
            response.setStatus(403);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.FORBIDDEN);
        }
        if(problemControl.NUMBER_OF_PROBLEMS < pCode) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.FORBIDDEN, "no problem found");
        }
        if(name.equals("")) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.BAD_REQUEST, "issue name not in format");
        }
        if(type < 0 || type > 5) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.BAD_REQUEST, "unknown issue type");
        }
        if(content.equals("")) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.BAD_REQUEST, "content not in format");
        }

        try {
            Issue issue = new Issue();
            issue.setIssueName(name);
            issue.setType(type);
            issue.setForProblem(pCode);
            issue.setContent(content);
            issue.setStatus(Issue.ISSUE_STATUS.OPEN);

            issueService.addNewIssue(issue, sessionService.getId(session));
            response.setStatus(201);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.CREATED);
        }
        catch (SQLException e) {
            response.setStatus(500);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.INTERNAL_SERVER_ERROR, "database error");
        }
    }

    @PatchMapping("/api/name/update")
    @ResponseBody
    public String updateTitle(@RequestParam("issue-code") int issueCode,
                              @RequestParam("new-name") String newName,
                              HttpSession session, HttpServletResponse response) {
        try {
            issueService.updateIssueTitle(issueCode, newName, sessionService.getId(session));
        }
        catch (ResourceNotFoundException e) {
            response.setStatus(404);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.NOT_FOUND, e.getMessage());
        }
        catch (SQLException e) {
            response.setStatus(500);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.INTERNAL_SERVER_ERROR, "database error");
        } catch (ForbiddenException e) {
            response.setStatus(403);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.FORBIDDEN, e.getMessage());
        }
        catch (IllegalArgumentException e) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.BAD_REQUEST, e.getMessage());
        }
        return RestfulReponse.createRestfulResponse(RestfulReponse.HTTP_CODE.OK);
    }

    @PatchMapping("/api/close")
    @ResponseBody
    public String closeIssue() {
        return "";
    }
}
