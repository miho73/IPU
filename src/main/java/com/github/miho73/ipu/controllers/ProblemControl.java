package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.library.security.SHA;
import com.github.miho73.ipu.repositories.SolutionRepository;
import com.github.miho73.ipu.repositories.UserRepository;
import com.github.miho73.ipu.services.ProblemService;
import com.github.miho73.ipu.services.ResourceService;
import com.github.miho73.ipu.services.SessionService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.logging.Logger;

@Controller("ProblemControl")
@RequestMapping("/problem")
public class ProblemControl {
    private final ProblemService problemService;
    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final ResourceService resourceService;
    private final SHA sha = new SHA();

    @Autowired
    public ProblemControl(ProblemService problemService, SessionService sessionService, UserRepository userRepository, ResourceService resourceService) {
        this.problemService = problemService;
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.resourceService = resourceService;
    }

    @GetMapping("")
    public String getProblemListPage(@RequestParam(value = "page", required = false, defaultValue = "0") String page, Model model, HttpSession session) {
        sessionService.loadSessionToModel(session, model);
        model.addAttribute("page", Integer.parseInt(page));
        return "problem/problemList";
    }

    @PostMapping(value = "/api/get", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String getProblemList(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        long frm = Long.parseLong(request.getParameter("frm"));
        long len = Long.parseLong(request.getParameter("len"));
        if(frm<=0 || len<=0 || len>=60) {
            response.sendError(400);
            return null;
        }
        JSONArray list = problemService.getProblemList(frm, len);
        return list.toString();
    }

    @GetMapping("/{pCode}")
    public String getProblem(@PathVariable("pCode") String code, Model model, HttpSession session) throws SQLException {
        Problem problem = problemService.getProblem(Long.parseLong(code));
        sessionService.loadSessionToModel(session, model);
        model.addAllAttributes(Map.of(
                "pCode", problem.getCode(),
                "pName", problem.getName(),
                "content", problem.getContent(),
                "solution", problem.getSolution(),
                "answer", problem.getAnswer(),
                "hint", problem.getHint(),
                "hasHint", problem.isHasHint(),
                "extrTabs", problem.getExternalTabs()
        ));
        return "problem/problemPage";
    }

    @GetMapping("/make")
    public String problemMake(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        sessionService.loadSessionToModel(session, model);
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        return "problem/mkProblem";
    }
    @PostMapping(value = "/make/upload", produces = "text/plain; charset=utf-8")
    @ResponseBody
    public String imageUpload(@RequestParam(value = "img")MultipartFile resource, HttpSession session) throws IOException, NoSuchAlgorithmException, SQLException {
        if(resource.getSize() > 5000000) {
            return "size";
        }
        byte[] res = resource.getBytes();
        String hash = sha.MD5(res);
        resourceService.addResource(res, hash, sessionService.getId(session));
        return hash;
    }
    @GetMapping(value = "/lib/{src}", produces = "application/octet-stream; charset=utf-8")
    @ResponseBody
    public byte[] resourceRequest(HttpServletResponse response, @PathVariable("src")String hash) throws SQLException, IOException {
        byte[] resource = resourceService.getResource(hash);
        if(resource == null) {
            response.sendError(404);
            return null;
        }
        return resource;
    }
    @PostMapping("/register")
    public String problemRegister(HttpServletRequest request, Model model, HttpSession session, HttpServletResponse response) throws SQLException, IOException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        Problem problem = new Problem();
        problem.setName        (request.getParameter("name"));
        problem.setCategory    (request.getParameter("cate"));
        problem.setDifficulty  (request.getParameter("diff"));
        problem.setContent     (request.getParameter("cont"));
        problem.setSolution    (request.getParameter("solu"));
        problem.setAnswer      (request.getParameter("answ"));
        problem.setHint        (request.getParameter("hint"));
        problem.setHasHint     (request.getParameter("hasH").equals("true"));
        problem.setExternalTabs(request.getParameter("extr"));
        problem.setTags        (request.getParameter("tags"));
        problemService.registerProblem(problem, session);
        return "redirect:/problem";
    }

    @GetMapping("/edit/{pCode}")
    public String editProblem(@PathVariable("pCode") String code, Model model, HttpSession session, HttpServletResponse response) throws IOException {
        sessionService.loadSessionToModel(session, model);
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        long pCode = Long.parseLong(code);
        model.addAttribute("prob_code", code);
        return "problem/editProblem";
    }

    @PostMapping(value = "/api/get-detail", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String problemDetail(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        long code = Long.parseLong(request.getParameter("code"));
        JSONObject detail = new JSONObject();
        Problem problem = problemService.getFullProblem(code);
        detail.put("cate", problem.getCategoryCode());
        detail.put("diff", problem.getDifficultyCode());
        detail.put("hashint", problem.isHasHint());
        detail.put("prob_ans", problem.getAnswer());
        detail.put("prob_cont", problem.getContent());
        detail.put("prob_exp", problem.getSolution());
        detail.put("prob_hint", problem.getHint());
        detail.put("prob_name", problem.getName());
        detail.put("spec", problem.getExternalTabs());
        detail.put("tags", problem.getTags());
        return detail.toString();
    }

    @PostMapping("/update")
    @ResponseBody
    public String problemUpdate(HttpServletRequest request, Model model, HttpSession session, HttpServletResponse response) throws SQLException, IOException {
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }
        Problem problem = new Problem();
        problem.setCode        (Long.parseLong(request.getParameter("code")));
        problem.setName        (request.getParameter("name"));
        problem.setCategory    (request.getParameter("cate"));
        problem.setDifficulty  (request.getParameter("diff"));
        problem.setContent     (request.getParameter("cont"));
        problem.setSolution    (request.getParameter("solu"));
        problem.setAnswer      (request.getParameter("answ"));
        problem.setHint        (request.getParameter("hint"));
        problem.setHasHint     (request.getParameter("hasH").equals("true"));
        problem.setExternalTabs(request.getParameter("extr"));
        problem.setTags        (request.getParameter("tags"));
        problemService.updateProblem(problem);
        return "/problem/"+problem.getCode();
    }

    @PostMapping("/api/solrep")
    @Transactional
    @ResponseBody
    //TODO: Implement transaction
    public String registerSolve(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws SQLException {
        if(!sessionService.checkLogin(session) || sessionService.hasPrivilege(SessionService.PRIVILEGES.USER, session)) {
            response.setStatus(403);
            return "forb";
        }

        String uid = sessionService.getId(session);
        Timestamp last_submit = (Timestamp) userRepository.getUserDataById(uid, "last_solve");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if(last_submit != null ){
             if((now.getTime()-last_submit.getTime())/1000 < 60) {
                 response.setStatus(429);
                 return "time";
             }
        }
        userRepository.updateUserTSById(uid, "last_solve", now);

        long code = Long.parseLong(request.getParameter("code"));
        long time = Long.parseLong(request.getParameter("time"));
        boolean result = request.getParameter("res").equals("1");
        long userCode = sessionService.getCode(session);
        problemService.registerSolution(code, time, result, userCode);
        return "ok";
    }
}
