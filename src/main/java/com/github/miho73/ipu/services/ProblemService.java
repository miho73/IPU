package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.exceptions.CannotJudgeException;
import com.github.miho73.ipu.library.ExperienceSystem;
import com.github.miho73.ipu.repositories.ProblemRepository;
import com.github.miho73.ipu.repositories.SolutionRepository;
import com.github.miho73.ipu.repositories.UserRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Service("ProblemService")
public class ProblemService {
    @Autowired private ProblemRepository problemRepository;
    @Autowired private SolutionRepository solutionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ExperienceSystem experienceSystem;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

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

    public void registerProblem(Problem problem) throws SQLException {
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

    public JSONObject registerSolution(int code, short time_took, JSONArray answer, int userCode) throws SQLException, CannotJudgeException {
        Connection probConnection = problemRepository.openConnection(),
                   solvesConnection = solutionRepository.openConnectionForEdit(),
                   userConnection = userRepository.openConnectionForEdit();
        try {
            Problem problem = problemRepository.getProblemSimple(code, probConnection);
            if(!problem.isActive()) {
                throw new CannotJudgeException("disabled_problem");
            }

            // Check last solve time
            Timestamp last_submit = (Timestamp) userRepository.getUserDataByCode(userCode, "last_solve", userConnection);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if(last_submit != null ){
                if((now.getTime()-last_submit.getTime())/1000 < 60) {
                    throw new CannotJudgeException("intermediate");
                }
            }
            userRepository.updateUserTSByCode(userCode, "last_solve", now, userConnection);

            // Judge
            JSONArray storedAnswer = new JSONArray(problem.getAnswer());
            if(storedAnswer.length() != answer.length()) {
                throw new CannotJudgeException("answer_format");
            }
            if(time_took >= 3600) {
                throw new CannotJudgeException("timeover");
            }
            short len = (short) storedAnswer.length();
            short corrects = 0;
            JSONArray judgeResults = new JSONArray();
            for(int i = 0; i<len; i++) {
                JSONObject sAnswer = storedAnswer.getJSONObject(i);
                JSONObject judgeResult = new JSONObject();
                if(sAnswer.getInt("method") == 0) {
                    boolean correct = answer.getInt(i) == 0;
                    judgeResult.put("acwa", correct);
                    if(correct) corrects++;
                }
                else {
                    boolean correct = sAnswer.getString("answer").equals(answer.get(i));
                    judgeResult.put("answer", sAnswer.getString("answer"));
                    judgeResult.put("yours", answer.get(i));
                    judgeResult.put("acwa", correct);
                    if(correct) corrects++;
                }
                judgeResults.put(judgeResult);
            }

            Problem.PROBLEM_DIFFICULTY difficulty = problem.getDifficulty();

            int solves = solutionRepository.getNumberOfSolves(userCode, code, solvesConnection);
            int exp = experienceSystem.getExp(difficulty, solves);
            exp = exp * corrects / len;
            solutionRepository.addSolution(userCode, code, time_took, judgeResults.toString(), corrects, len, exp, solvesConnection);
            userRepository.addExperience(exp, userCode, userConnection);

            userRepository.commit(userConnection);
            solutionRepository.commit(solvesConnection);

            JSONObject judgeReply = new JSONObject();
            judgeReply.put("result", judgeResults);
            judgeReply.put("corrects", corrects);
            judgeReply.put("total", len);
            judgeReply.put("exp", exp);
            return judgeReply;
        }
        catch (SQLException | CannotJudgeException e) {
            if(e.getMessage().equals("intermediate")) userRepository.commit(userConnection);
            else userRepository.rollback(userConnection);
            solutionRepository.rollback(solvesConnection);
            throw e;
        } finally {
            solutionRepository.close(solvesConnection);
            problemRepository.close(probConnection);
            userRepository.close(userConnection);
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
        int PROBLEM_PER_PAGE = 30;
        int frm = page* PROBLEM_PER_PAGE +1;

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

    public int getNumberOfProblems() throws SQLException {
        Connection connection = problemRepository.openConnection();
        int cnt = problemRepository.getNumberOfProblems(connection);
        problemRepository.close(connection);
        return cnt;
    }

    public int getRandomProblemInBranch(String category) throws SQLException {
        Connection connection = problemRepository.openConnection();
        int pCode = problemRepository.getRandomProblemInBranch(category, connection);
        problemRepository.close(connection);
        return pCode;
    }
}
