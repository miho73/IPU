package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.Issue;
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
import java.util.List;
import java.util.Map;

@Controller("IssueController")
@RequestMapping("issue")
public class IssueController {
    @Autowired SessionService sessionService;
    @Autowired IssueService issueService;
    @Autowired ProblemControl problemControl;

    private final int ISSUE_PER_PAGE = 30;

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

    @GetMapping("/new")
    public String createNewIssuePage(Model model, HttpSession session) {
        if(!sessionService.checkLogin(session)) {
            return "redirect:/login/?ret=/issue/new";
        }
        sessionService.loadSessionToModel(session, model);
        return "issue/createNewIssue";
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

    @PatchMapping("/api/close")
    @ResponseBody
    public String closeIssue() {
        return "";
    }
}
