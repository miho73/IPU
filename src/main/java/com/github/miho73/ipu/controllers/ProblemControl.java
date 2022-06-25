package com.github.miho73.ipu.controllers;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.exceptions.CannotJudgeException;
import com.github.miho73.ipu.library.ipuac.Renderer;
import com.github.miho73.ipu.library.rest.response.RestfulReponse;
import com.github.miho73.ipu.library.security.SHA;
import com.github.miho73.ipu.services.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;

@Controller("ProblemControl")
public class ProblemControl {
    @Autowired private ProblemService problemService;
    @Autowired private SessionService sessionService;
    @Autowired private UserService userService;
    @Autowired private ResourceService resourceService;
    @Autowired private TagService tagService;
    @Autowired private Renderer renderer = new Renderer();
    @Autowired private SHA sha = new SHA();

    @Value("${ipu.access.force-login-for-problem}") private boolean FORCE_LOGIN_FOR_PROBLEM;
    @Value("${ipu.judge.max-judge-for-problem}") private int MAX_JUDGES;

    public int NUMBER_OF_PROBLEMS;
    public int PROBLEM_PER_PAGE = 30;
    private final List<String> acceptedBranches = Arrays.asList("alge", "numb", "comb", "geom", "phys", "chem", "biol", "eart");
    private final List<String> acceptedDifficulty = Arrays.asList("unse", "unra", "broz", "silv", "gold", "sapp", "ruby", "diam");

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @PostConstruct
    public void initProblemCount() throws SQLException {
        NUMBER_OF_PROBLEMS = problemService.getNumberOfProblems();
        LOGGER.debug("Problem count initialized to "+NUMBER_OF_PROBLEMS);
    }

    // Get problem list
    @GetMapping("/problem")
    public String getProblemListPage(Model model, HttpSession session, HttpServletResponse response,
                                     @RequestParam(value = "page", required = false, defaultValue = "0") int page) throws IOException, SQLException {

        sessionService.loadSessionToModel(session, model);
        int frm = page*PROBLEM_PER_PAGE+1;
        if(frm<=0) {
            response.sendError(400);
            return null;
        }
        JSONArray list = problemService.getProblemList(frm, PROBLEM_PER_PAGE);
        JSONArray processedList = tagService.processTagsToHtml(list, session);

        model.addAttribute("pList", processedList.toList());
        model.addAttribute("nothing", processedList.length()==0);
        model.addAttribute("hasPrev", page != 0);
        model.addAttribute("hasNext", (NUMBER_OF_PROBLEMS-(page+1)*PROBLEM_PER_PAGE)>0);
        model.addAttribute("page", page);
        model.addAttribute("pages", Math.ceil((float)NUMBER_OF_PROBLEMS/PROBLEM_PER_PAGE));
        return "problem/problemList";
    }

    // redirect to the latest problem page
    @GetMapping("/problem/latest")
    public String getLastPageOfProblem(Model model, HttpSession session, HttpServletResponse response) {
        int page = (int) Math.floor((float)NUMBER_OF_PROBLEMS/PROBLEM_PER_PAGE);
        return "redirect:/problem/?page="+page;
    }

    // get problem category page
    @GetMapping("/problem/category")
    public String getProblemCategoryPage(Model model, HttpSession session) throws SQLException {

        sessionService.loadSessionToModel(session, model);
        Hashtable<Problem.PROBLEM_CATEGORY, Integer> dat = problemService.getNumberOfProblemsInCategory();
        model.addAllAttributes(Map.of(
                "alge", dat.get(Problem.PROBLEM_CATEGORY.ALGEBRA),
                "biol", dat.get(Problem.PROBLEM_CATEGORY.BIOLOGY),
                "comb", dat.get(Problem.PROBLEM_CATEGORY.COMBINATORICS),
                "chem", dat.get(Problem.PROBLEM_CATEGORY.CHEMISTRY),
                "numb", dat.get(Problem.PROBLEM_CATEGORY.NUMBER_THEORY),
                "phys", dat.get(Problem.PROBLEM_CATEGORY.PHYSICS),
                "geom", dat.get(Problem.PROBLEM_CATEGORY.GEOMETRY),
                "eart", dat.get(Problem.PROBLEM_CATEGORY.EARTH_SCIENCE)
        ));
        return "problem/problemCategory";
    }

    // process search query
    @GetMapping("/problem/search")
    public String searchBySearch(Model model, HttpSession session, HttpServletResponse response,
                                 @RequestParam(value = "page", required = false, defaultValue = "0") int page, //TODO: implement page feature
                                 @RequestParam(value = "cont", required = false, defaultValue = "") String contains,
                                 @RequestParam(value = "diff", required = false, defaultValue = "") String difficulty,
                                 @RequestParam(value = "cate", required = false, defaultValue = "") String category) throws SQLException, IOException {

        // check query before process
        if(contains.length() >= 100) {
            response.sendError(400);
            return null;
        }
        if(!category.equals("") && acceptedBranches.contains(category)) {
            response.sendError(400);
            return null;
        }
        if(!difficulty.equals("") && acceptedDifficulty.contains(difficulty)) {
            response.sendError(400);
            return null;
        }

        sessionService.loadSessionToModel(session, model);
        model.addAttribute("page", page);
        JSONArray sResult = problemService.searchProblem(page, contains, category, difficulty);
        JSONArray processedResult = tagService.processTagsToHtml(sResult, session);

        model.addAttribute("pList", processedResult.toList());
        model.addAttribute("query", contains);
        model.addAttribute("nothing", processedResult.length()==0);
        model.addAttribute("cate", category);
        return "problem/problemSearch";
    }

    // redirect to random problem
    @GetMapping("/problem/random")
    public void getRandomProblem(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        sessionService.loadSessionToModel(session, model);
        Random rand = new Random();
        rand.setSeed(System.nanoTime());
        response.sendRedirect("/problem/"+(rand.nextInt(NUMBER_OF_PROBLEMS)+1));
    }

    // get random problem in branch
    @GetMapping("/problem/random/branch")
    public void getRandomProblemInBranch(Model model, HttpSession session, HttpServletResponse response,
                                         @RequestParam("branch") String branch) throws IOException, SQLException {

        sessionService.loadSessionToModel(session, model);
        if(!acceptedBranches.contains(branch)) {
            response.sendError(400);
            return;
        }
        int pCode = problemService.getRandomProblemInBranch(branch);
        if(pCode == -1) {
            response.sendRedirect("/problem/search/?cate="+branch);
        }
        else {
            response.sendRedirect("/problem/"+pCode);
        }
    }

    // get problem
    @GetMapping("/problem/{pCode}")
    public String getProblem(@PathVariable("pCode") String code, Model model, HttpSession session, HttpServletResponse response) throws IOException {
        try {
            // if [force login for problem] is true, forbidden when request is not signed in
            if(FORCE_LOGIN_FOR_PROBLEM && !sessionService.checkLogin(session)) {
                return "redirect:/login/?ret=/problem/"+code;
            }

            // query problem from database
            Problem problem = problemService.getProblem(Integer.parseInt(code));
            if(problem == null) {
                response.sendError(404);
                return null;
            }
            boolean isStared = (sessionService.checkLogin(session) && userService.isUserStaredProblem(sessionService.getCode(session), Integer.parseInt(code)));

            sessionService.loadSessionToModel(session, model);
            model.addAllAttributes(Map.of(
                    "pCode", problem.getCode(),
                    "pName", problem.getName(),
                    "active", problem.isActive(),
                    "problem_ipuac", renderer.IPUACtoHTML(problem.getContent()),
                    "solution_ipuac", renderer.IPUACtoHTML(problem.getSolution()),
                    "tags", tagService.processTagsToHtml(problem),
                    "answer", problem.getAnswer().toList(),
                    "stared", isStared
            ));
            return "problem/problemPage";
        }
        catch (SQLException e) {
            response.sendError(500);
            LOGGER.error("Cannot open problem '" +code+"'", e);
            return null;
        }
    }

    // translate IPUAC code to HTML
    @PostMapping(value = "/api/problem/ipuac-translation", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String ipuacTranslation(HttpSession session, HttpServletResponse response,
                                   @RequestParam("code") String ipuac) {

        // problem make required to use api
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.setStatus(403);
            return RestfulReponse.createRestfulResponse(HttpStatus.FORBIDDEN);
        }

        // translate all requested codes
        JSONArray codes = new JSONArray(ipuac);
        JSONArray html = new JSONArray();
        for(int i=0; i<codes.length(); i++) {
            html.put(renderer.IPUACtoHTML(codes.getString(i)));
        }
        return RestfulReponse.createRestfulResponse(HttpStatus.OK, html);
    }

    // get make problem page
    @GetMapping("/problem/make")
    public String problemMake(Model model, HttpSession session, HttpServletResponse response) throws IOException {
        // problem make required to get page
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }

        sessionService.loadSessionToModel(session, model);
        return "problem/mkProblem";
    }

    // api to upload image
    @PostMapping(value = "/api/resource/post", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String imageUpload(HttpSession session, HttpServletResponse response,
                              @RequestParam("img") MultipartFile resource) throws IOException {

        // problem make required to upload image
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.setStatus(403);
            return RestfulReponse.createRestfulResponse(HttpStatus.FORBIDDEN);
        }

        // if file size exceed 1MB
        if(resource.getSize() > 1000000) {
            response.setStatus(400);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "file too large");
        }

        // insert into database
        try {
            byte[] res = resource.getBytes();
            String hash = sha.MD5(res); // hash as resource uuid
            resourceService.addResource(res, hash, sessionService.getId(session));
            return RestfulReponse.createRestfulResponse(HttpStatus.OK, hash);
        }
        catch (SQLException e) {
            response.setStatus(500);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR, "database error");
        }
        catch (NoSuchAlgorithmException e) {
            response.setStatus(500);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR, "hash failure");
        }
    }

    // get resource
    @GetMapping(value = "/resource/get/{src}", produces = "application/octet-stream; charset=utf-8")
    @ResponseBody
    public byte[] resourceRequest(HttpServletResponse response, HttpSession session,
                                  @PathVariable("src") String hash) throws SQLException, IOException {

        // user must have privilege to see problem
        if(FORCE_LOGIN_FOR_PROBLEM && !sessionService.checkLogin(session)) {
            response.sendError(403);
            return null;
        }

        // query resource
        byte[] resource = resourceService.getResource(hash);

        // resource not found
        if(resource == null) {
            response.sendError(404);
            return null;
        }

        return resource;
    }

    /**
     * check if answer form is valid
     * @param ansJson answer form(in json) to validate
     * @return true when INVALID
     */
    public boolean validateAnswerJson(String ansJson) {
        //answer json too long
        if(ansJson.length() > 2000) {
            return true;
        }

        // separate answer json into each problem judge
        JSONArray tj = new JSONArray(ansJson);

        // error when judge count exceed judge limit
        if(tj.length() > MAX_JUDGES) return true;

        for(Object judgeObject : tj) {
            JSONObject judge = (JSONObject)judgeObject;
            // check if judge has method and name
            if(!(judge.has("method") && judge.has("name"))) return true;

            // check if method is 0, 1, 2
            if(!(0<=judge.getInt("method") && 2>=judge.getInt("method"))) return true;

            // if judge has answer, check its length
            if(judge.has("answer")) {
                if(judge.getString("answer").length() > 100) {
                    return true;
                }
            }
        }
        // return OK when all checks pass
        return false;
    }

    // api to register problem
    @PostMapping(value = "/problem/post", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String problemRegister(HttpServletRequest request, Model model, HttpSession session, HttpServletResponse response) {
        // problem make required to register problem
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.setStatus(404);
            return RestfulReponse.createRestfulResponse(HttpStatus.FORBIDDEN);
        }

        try {
            String answer = request.getParameter("answer");

            // validate answer json
            if(validateAnswerJson(answer)) {
                response.setStatus(400);
                return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "anjs");
            }

            // form -> problem object
            Problem problem = new Problem();
            problem.setName         (request.getParameter("name"));
            problem.setCategory     (request.getParameter("cate"));
            problem.setDifficulty   (request.getParameter("diff"));
            problem.setContent      (request.getParameter("cont"));
            problem.setSolution     (request.getParameter("solu"));
            problem.setTags         (request.getParameter("tags"));
            problem.setAnswer       (answer);
            problem.setActive       (Boolean.parseBoolean(request.getParameter("active")));
            problem.setAuthor_name  (sessionService.getName(session));

            // insert into database
            problemService.registerProblem(problem);

            // increase problem count
            NUMBER_OF_PROBLEMS++;
            LOGGER.debug("Problem registered. Problem count now set to "+NUMBER_OF_PROBLEMS);

            response.setStatus(201);
            return RestfulReponse.createRestfulResponse(HttpStatus.CREATED, NUMBER_OF_PROBLEMS);
        }
        catch (SQLException e) {
            response.setStatus(500);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR, "dber");
        }
    }

    // edit problem page get
    @GetMapping("/problem/edit/{pCode}")
    public String editProblem(@PathVariable("pCode") String code, Model model, HttpSession session, HttpServletResponse response) throws IOException {
        // problem make required to edit problem
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            response.sendError(404);
            return null;
        }

        sessionService.loadSessionToModel(session, model);
        model.addAttribute("prob_code", code);
        return "problem/editProblem";
    }

    // general api to get problem data
    @GetMapping(value = "/api/problem/get", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String problemDetail(HttpServletRequest request, HttpServletResponse response, HttpSession session,
                                @RequestParam("code") int code) {

        try {
            // user can view problem to use this api
            if(FORCE_LOGIN_FOR_PROBLEM && !sessionService.checkLogin(session)) {
                response.setStatus(403);
                return RestfulReponse.createRestfulResponse(HttpStatus.FORBIDDEN, "you must be logged in");
            }

            // query problem
            Problem problem = problemService.getFullProblem(code);
            if(problem == null) {
                response.setStatus(404);
                return RestfulReponse.createRestfulResponse(HttpStatus.NOT_FOUND, "resource not found");
            }

            JSONObject detail = new JSONObject();
            detail.put("cate", problem.getCategoryCode());
            detail.put("diff", problem.getDifficultyCode());
            detail.put("prob_cont", problem.getContent());
            detail.put("prob_exp", problem.getSolution());
            detail.put("prob_name", problem.getName());
            detail.put("tags", problem.getTags());
            detail.put("active", problem.isActive());
            detail.put("answer", problem.getAnswer().toString());
            return RestfulReponse.createRestfulResponse(HttpStatus.OK, detail);
        }
        catch (SQLException e) {
            response.setStatus(500);
            LOGGER.error("problem get api error", e);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR, "database error");
        }
    }

    // api to update problem
    @PutMapping("/problem/update")
    @ResponseBody
    public String problemUpdate(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        // problem make required to update problem
        if(sessionService.hasPrivilege(SessionService.PRIVILEGES.PROBLEM_MAKE, session)) {
            String ret = RestfulReponse.createRestfulResponse(HttpStatus.FORBIDDEN);
            response.sendError(403);
            return ret;
        }

        try {
            String answer = request.getParameter("answer");

            // check if new answer json is valid
            if(validateAnswerJson(answer)) {
                response.setStatus(400);
                return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "anjs");
            }

            // form -> object
            Problem problem = new Problem();
            problem.setCode         (Integer.parseInt(request.getParameter("code")));
            problem.setName         (request.getParameter("name"));
            problem.setCategory     (request.getParameter("cate"));
            problem.setDifficulty   (request.getParameter("diff"));
            problem.setContent      (request.getParameter("cont"));
            problem.setSolution     (request.getParameter("solu"));
            problem.setTags         (request.getParameter("tags"));
            problem.setAnswer       (answer);
            problem.setActive       (Boolean.parseBoolean(request.getParameter("active")));

            // update database
            problemService.updateProblem(problem);
            return RestfulReponse.createRestfulResponse(HttpStatus.OK);
        } catch (SQLException e) {
            response.setStatus(500);
            LOGGER.error("cannot update problem", e);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR, "dber");
        }
    }

    @PostMapping("/api/problem/solution/post")
    @ResponseBody
    public String registerSolve(HttpServletRequest request, HttpSession session, HttpServletResponse response,
                                @RequestParam("code") int code,
                                @RequestParam("time") short time,
                                @RequestParam(value = "answer") String answerText) {

        try {
            // user must be signed in to submit solution
            if(!sessionService.checkLogin(session) || sessionService.hasPrivilege(SessionService.PRIVILEGES.USER, session)) {
                response.setStatus(403);
                return RestfulReponse.createRestfulResponse(HttpStatus.FORBIDDEN, "forb");
            }

            // check solution json length
            if(answerText.length() > 1200) {
                response.setStatus(413);
                return RestfulReponse.createRestfulResponse(HttpStatus.FORBIDDEN, "astl");
            }

            // register solution
            int uCode = sessionService.getCode(session);
            JSONArray answer = new JSONArray(answerText);
            JSONObject result = problemService.registerSolution(code, time, answer, uCode);
            return RestfulReponse.createRestfulResponse(HttpStatus.OK, result);

        } catch (CannotJudgeException e) {
            response.setStatus(500);
            String msg;
            switch (e.getMessage()) {
                case "disabled_problem" -> {
                    msg = "dis";
                    response.setStatus(400);
                    return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, msg);
                }
                case "intermediate" -> {
                    msg = "intr";
                    response.setStatus(429);
                    return RestfulReponse.createRestfulResponse(HttpStatus.TOO_MANY_REQUESTS, msg);
                }
                case "answer_format" -> {
                    msg = "ansf";
                    response.setStatus(400);
                    return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, msg);
                }
                case "timeover" -> {
                    msg = "tiov";
                    response.setStatus(400);
                    return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, msg);
                }
                default -> {
                    msg = "unkn";
                    response.setStatus(500);
                    return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR, msg);
                }
            }
        }
        catch (SQLException e) {
            response.setStatus(500);
            LOGGER.error("Cannot register solution due to database error. ", e);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR, "dber");
        }
        catch (JSONException e) {
            response.setStatus(500);
            LOGGER.error("Cannot register solution due to parsing exception. ", e);
            return RestfulReponse.createRestfulResponse(HttpStatus.BAD_REQUEST, "pras");
        }
    }

    // api to change star
    @PatchMapping(value = "/problem/api/change-star", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String updateStar(HttpSession session,
                             @RequestParam("code") int code, HttpServletResponse response) {

        LOGGER.debug("Change star of user "+sessionService.getId(session)+". problem of "+code);

        // user must be signed in and can solve problem
        if(!sessionService.checkLogin(session) || sessionService.hasPrivilege(SessionService.PRIVILEGES.USER, session)) {
            response.setStatus(403);
            return RestfulReponse.createRestfulResponse(HttpStatus.FORBIDDEN);
        }

        try {
            // change star
            int result = userService.changeUserStar(sessionService.getCode(session), code);
            JSONObject res = new JSONObject();
            res.put("stared", result);
            return RestfulReponse.createRestfulResponse(HttpStatus.OK, res);
        } catch (SQLException e) {
            response.setStatus(500);
            LOGGER.error("cannot change star", e);
            return RestfulReponse.createRestfulResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
