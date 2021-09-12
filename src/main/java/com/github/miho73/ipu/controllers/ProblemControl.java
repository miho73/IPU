package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.domain.ProblemListAPI;
import com.github.miho73.ipu.services.ProblemService;
import com.github.miho73.ipu.services.SessionService;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@Controller("ProblemControl")
@RequestMapping("/problem")
public class ProblemControl {
    private final ProblemService problemService;
    private final SessionService sessionService;

    @Autowired
    public ProblemControl(ProblemService problemService, SessionService sessionService) {
        this.problemService = problemService;
        this.sessionService = sessionService;
    }

    @GetMapping("")
    public String getProblemListPage(@RequestParam(value = "page", required = false, defaultValue = "0") String page, Model model, HttpSession session) {
        sessionService.loadSessionToModel(session, model);
        model.addAttribute("page", Integer.parseInt(page));
        return "problem/problemList";
    }

    @PostMapping("/api/get")
    @ResponseBody
    public String getProblemList(@ModelAttribute ProblemListAPI pla, HttpServletResponse response) throws SQLException {
        response.setContentType("application/json");
        JSONArray list = problemService.getProblemList(pla.frm, pla.len);
        return list.toString();
    }

    @GetMapping("/{pCode}")
    public String getProblem(@PathVariable("pCode") String code, Model model, HttpSession session) throws SQLException {
        Problem problem = problemService.getProblem(Integer.parseInt(code));
        sessionService.loadSessionToModel(session, model);
        model.addAllAttributes(Map.of(
                "pCode", problem.getCode(),
                "pName", problem.getName(),
                "content", problem.getContent(),
                "solution", problem.getSolution(),
                "answer", problem.getAnswer(),
                "hint", problem.getHint(),
                "hasHint", problem.isHasHint(),
                "extrTabs", problem.getExtrTabs()
        ));
        return "problem/problemPage";
    }

    @GetMapping("/make")
    public String problemMake(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        if(!sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            return "redirect:/problem";
        }
        return "problem/mkProblem";
    }
}
