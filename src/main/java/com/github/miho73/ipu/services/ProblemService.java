package com.github.miho73.ipu.services;

import com.github.miho73.ipu.library.Converters;
import com.github.miho73.ipu.library.ExperienceSystem;
import com.github.miho73.ipu.repositories.ProblemRepository;
import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.repositories.SolutionRepository;
import com.github.miho73.ipu.repositories.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.Connection;
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

    private final Converters converters = new Converters();

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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
        Connection connection = problemRepository.openConnection();
        List<Problem> problems = problemRepository.getProblemBriefly(from, length, connection);
        problemRepository.close(connection);
        for(Problem problem : problems) {
            JSONObject element = new JSONObject();
            element.put("code", problem.getCode());
            element.put("name", problem.getName());
            JSONArray tags = new JSONArray(problem.getTags());
            tags.put(new JSONObject(Map.of("key", "diff", "content", problem.getDifficultyCode())));
            tags.put(new JSONObject(Map.of("key", "cate", "content", problem.getCategoryCode())));
            element.put("tags", tags);
            element.put("active", problem.isActive());
            root.put(element);
        }
        return root;
    }

    public Problem getProblem(int code) throws SQLException {
        Connection connection = problemRepository.openConnection();
        Problem problem = problemRepository.getProblem(code, connection);
        problemRepository.close(connection);
        return problem;
    }
    public Problem getFullProblem(int code) throws SQLException {
        Connection connection = problemRepository.openConnection();
        Problem problem = problemRepository.getFullProblem(code, connection);
        problemRepository.close(connection);
        return problem;
    }

    public void registerProblem(Problem problem, HttpSession session) throws SQLException {
        Connection connection = problemRepository.openConnectionForEdit();
        try {
            problemRepository.registerProblem(problem, connection);
            problemRepository.commitAndClose(connection);
        }
        catch (Exception e) {
            problemRepository.rollbackAndClose(connection);
            LOGGER.error("Cannot register problem", e);
            throw e;
        }
    }
    public void updateProblem(Problem problem) throws SQLException {
        Connection connection = problemRepository.openConnectionForEdit();
        try {
            problemRepository.updateProblem(problem, connection);
            problemRepository.commitAndClose(connection);
        }
        catch (Exception e) {
            problemRepository.rollbackAndClose(connection);
            LOGGER.error("Cannot update problem", e);
            throw e;
        }
    }

    public void registerSolution(int code, int time, boolean result, int userCode, Connection usrConnection) throws SQLException {
        Connection probConnection = problemRepository.openConnection(),
                   solvesConnection = solutionRepository.openConnectionForEdit();
        try {
            Problem problem = problemRepository.getProblemSimple(code, probConnection);
            if(!problem.isActive()) {
                return;
            }
            solutionRepository.addSolution(code, time, result, userCode, solvesConnection);
            Problem.PROBLEM_DIFFICULTY difficulty = problem.getDifficulty();
            int solves = solutionRepository.getNumberOfSolves(userCode, code, solvesConnection);
            int exp = experienceSystem.getExp(difficulty, solves);
            if(!result) exp=experienceSystem.toWa(exp, difficulty);
            userRepository.addExperience(exp, userCode, usrConnection);
            userRepository.commit(usrConnection);
            solutionRepository.commit(solvesConnection);
        }
        catch (Exception e) {
            solutionRepository.rollback(solvesConnection);
            LOGGER.error("Cannot register new solution(error while completing db transaction)", e);
            throw e;
        }
        finally {
            solutionRepository.close(solvesConnection);
            problemRepository.close(probConnection);
        }
    }

    public Hashtable<Problem.PROBLEM_CATEGORY, Integer> getNumberOfProblemsInCategory() throws SQLException {
        Hashtable<Problem.PROBLEM_CATEGORY, Integer> ret = new Hashtable<>();

        Connection connection = problemRepository.openConnection();

        ret.put(Problem.PROBLEM_CATEGORY.ALGEBRA,        problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.ALGEBRA, connection));
        ret.put(Problem.PROBLEM_CATEGORY.BIOLOGY,        problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.BIOLOGY, connection));
        ret.put(Problem.PROBLEM_CATEGORY.CHEMISTRY,      problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.CHEMISTRY, connection));
        ret.put(Problem.PROBLEM_CATEGORY.COMBINATORICS,  problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.COMBINATORICS, connection));
        ret.put(Problem.PROBLEM_CATEGORY.EARTH_SCIENCE,  problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.EARTH_SCIENCE, connection));
        ret.put(Problem.PROBLEM_CATEGORY.GEOMETRY,       problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.GEOMETRY, connection));
        ret.put(Problem.PROBLEM_CATEGORY.NUMBER_THEORY, problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.NUMBER_THEORY, connection));
        ret.put(Problem.PROBLEM_CATEGORY.PHYSICS,        problemRepository.getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY.PHYSICS, connection));

        problemRepository.close(connection);
        return ret;
    }

    public JSONArray searchProblem(int page, String has, String cate, String diff) throws SQLException {
        int frm = page*PROBLEM_PER_PAGE+1;

        Vector<String> wheres = new Vector<>();
        Map<String, String> hasAndValues = new Hashtable<>();
        if(!has.equals("")) {
            wheres.add("(problem_name LIKE ? OR problem_content LIKE ?)");
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

        Connection connection =  problemRepository.openConnection();

        JSONArray root = new JSONArray();
        List<Problem> problems = problemRepository.searchProblem(frm, PROBLEM_PER_PAGE, hasAndValues, where, connection);

        problemRepository.close(connection);

        for(Problem problem : problems) {
            JSONObject element = new JSONObject();
            element.put("code", problem.getCode());
            element.put("name", problem.getName());
            JSONArray tags = new JSONArray(problem.getTags());
            tags.put(new JSONObject(Map.of("key", "diff", "content", problem.getDifficultyCode())));
            tags.put(new JSONObject(Map.of("key", "cate", "content", problem.getCategoryCode())));
            element.put("tags", tags);
            element.put("active", problem.isActive());
            root.put(element);
        }
        return root;
    }

    public JSONArray processTagsToHtml(JSONArray sResult) {
        JSONArray processedResult = new JSONArray();
        sResult.forEach(result->{
            JSONObject workingProblem = ((JSONObject)result);
            JSONArray tags = (JSONArray)workingProblem.get("tags");
            StringBuilder html = new StringBuilder();
            tags.forEach((tagx)->{
                JSONObject tag = (JSONObject)tagx;
                Object key = tag.get("key");
                if ("diff".equals(key)) {
                    html.append("<span class=\"tag tag-diff\" style=\"background-color: ")
                        .append(converters.convertDiffColor(tag.getString("content")))
                        .append(";\">")
                        .append(converters.convertDiff(tag.getString("content")))
                        .append("</span>");
                } else if ("cate".equals(key)) {
                    html.append("<span class=\"tag tag-cate\">")
                        .append(converters.convertSubj(tag.getString("content")))
                        .append("</span>");
                } else {
                    html.append("<span class=\"tag tag-custom\" style=\"background-color: #")
                        .append(tag.get("back"))
                        .append("; color: #")
                        .append(tag.get("color"))
                        .append(";\">")
                        .append(tag.get("content"))
                        .append("</span>");
                }
            });
            if(!workingProblem.getBoolean("active")) {
                html.append("<span class=\"tag tag-cannot-solve\">제출 불가</span>");
            }
            workingProblem.put("tags", html.toString());
            processedResult.put(workingProblem);
        });
        return processedResult;
    }

    public int getNumberOfProblems() throws SQLException {
        Connection connection = problemRepository.openConnection();
        int cnt = problemRepository.getNumberOfProblems(connection);
        connection.close();
        return cnt;
    }
}
