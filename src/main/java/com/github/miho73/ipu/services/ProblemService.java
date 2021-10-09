package com.github.miho73.ipu.services;

import com.github.miho73.ipu.library.ExperienceSystem;
import com.github.miho73.ipu.repositories.ProblemRepository;
import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.repositories.SolutionRepository;
import com.github.miho73.ipu.repositories.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Service("ProblemService")
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final SolutionRepository solutionRepository;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final ExperienceSystem experienceSystem;

    private final int PROBLEM_PER_PAGE = 30;

    @Autowired
    public ProblemService(ProblemRepository problemRepository, SessionService sessionService, SolutionRepository solutionRepository, UserRepository userRepository, ExperienceSystem experienceSystem) {
        this.problemRepository = problemRepository;
        this.sessionService = sessionService;
        this.solutionRepository = solutionRepository;
        this.userRepository = userRepository;
        this.experienceSystem = experienceSystem;
    }

    public JSONArray getProblemList(int from, int length) throws SQLException {
        JSONArray root = new JSONArray();
        List<Problem> problems = problemRepository.getProblemBriefly(from, length);
        for(Problem problem : problems) {
            JSONObject element = new JSONObject();
            element.put("code", problem.getCode());
            element.put("name", problem.getName());
            JSONArray tags = new JSONArray(problem.getTags());
            tags.put(new JSONObject(Map.of("key", "diff", "content", problem.getDifficultyCode())));
            tags.put(new JSONObject(Map.of("key", "cate", "content", problem.getCategoryCode())));
            element.put("tags", tags);
            root.put(element);
        }
        return root;
    }

    public Problem getProblem(int code) throws SQLException {
        return problemRepository.getProblem(code);
    }
    public Problem getFullProblem(int code) throws SQLException {
        return problemRepository.getFullProblem(code);
    }

    public void registerProblem(Problem problem, HttpSession session) throws SQLException {
        problemRepository.registerProblem(problem, sessionService.getName(session));
    }
    public void updateProblem(Problem problem) throws SQLException {
        problemRepository.updateProblem(problem);
    }

    public void registerSolution(int code, int time, boolean result, int userCode) throws SQLException {
        solutionRepository.addSolution(code, time, result, userCode);
        Problem.PROBLEM_DIFFICULTY difficulty = problemRepository.getProblemSimple(code).getDifficulty();
        int solves = solutionRepository.getNumberOfSolves(userCode, code);
        int exp = experienceSystem.getExp(difficulty, solves);
        if(!result) exp=experienceSystem.toWa(exp, difficulty);
        userRepository.addExperience(exp, userCode);
    }

    public Hashtable<Problem.PROBLEM_CATEGORY, Integer> getNumberOfProblemsInCategory() throws SQLException {
        Hashtable<Problem.PROBLEM_CATEGORY, Integer> ret = new Hashtable<>();

        ret.put(Problem.PROBLEM_CATEGORY.ALGEBRA,        problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.ALGEBRA));
        ret.put(Problem.PROBLEM_CATEGORY.BIOLOGY,        problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.BIOLOGY));
        ret.put(Problem.PROBLEM_CATEGORY.CHEMISTRY,      problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.CHEMISTRY));
        ret.put(Problem.PROBLEM_CATEGORY.COMBINATORICS,  problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.COMBINATORICS));
        ret.put(Problem.PROBLEM_CATEGORY.EARTH_SCIENCE,  problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.EARTH_SCIENCE));
        ret.put(Problem.PROBLEM_CATEGORY.GEOMETRY,       problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.GEOMETRY));
        ret.put(Problem.PROBLEM_CATEGORY.NUMBER_THEORY, problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.NUMBER_THEORY));
        ret.put(Problem.PROBLEM_CATEGORY.PHYSICS,        problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.PHYSICS));

        return ret;
    }

    public JSONArray searchProblem(int page, String has, String cate, String diff) throws SQLException {
        int frm = page*PROBLEM_PER_PAGE+1;

        Vector<String> wheres = new Vector<>();
        Map<String, String> hasAndValues = new Hashtable<>();
        if(!has.equals("")) {
            wheres.add("(problem_name LIKE ? OR problem_content LIKE ? OR problem_solution LIKE ?)");
            hasAndValues.put("has", has);
        }
        if(!cate.equals("")) {
            wheres.add("problem_category = ?");
            hasAndValues.put("cat", cate);
        }
        if(!diff.equals("")) {
            wheres.add("problem_difficulty = ?");
            hasAndValues.put("dif", diff);
        }

        String where = String.join(" AND ", wheres);
        if(!where.equals("")) where = "AND "+where;

        JSONArray root = new JSONArray();
        List<Problem> problems = problemRepository.searchProblem(frm, PROBLEM_PER_PAGE, hasAndValues, where);
        for(Problem problem : problems) {
            JSONObject element = new JSONObject();
            element.put("code", problem.getCode());
            element.put("name", problem.getName());
            JSONArray tags = new JSONArray(problem.getTags());
            tags.put(new JSONObject(Map.of("key", "diff", "content", problem.getDifficultyCode())));
            tags.put(new JSONObject(Map.of("key", "cate", "content", problem.getCategoryCode())));
            element.put("tags", tags);
            root.put(element);
        }
        return root;
    }
}
