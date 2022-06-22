package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.domain.User;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Stream;

@Service("UserService")
public class UserService {
    @Autowired private UserRepository userRepository;
    @Autowired private ProblemRepository problemRepository;
    @Autowired private SolutionRepository solutionRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public User getUserByCode(int code) throws SQLException {
        Connection connection = userRepository.openConnection();
        User user = userRepository.getUserByCode(code, connection);
        userRepository.close(connection);
        return  user;
    }
    public Object getUserDataById(String id, String column) throws SQLException {
        Connection connection = userRepository.openConnection();
        Object dat = userRepository.getUserDataById(id, column, connection);
        userRepository.close(connection);
        return dat;
    }
    public void updateStringById(String id, String column, String nData) throws SQLException {
        Connection connection = userRepository.openConnectionForEdit();
        try {
            userRepository.updateUserStringById(id, column, nData, connection);
            userRepository.commitAndClose(connection);
        }
        catch (Exception e) {
            userRepository.rollbackAndClose(connection);
            LOGGER.error("Cannot update user data", e);
            throw e;
        }
    }

    public List<User> getUserRanking(int len) throws SQLException {
        Connection connection = userRepository.openConnection();
        List<User> users =  userRepository.getUserRanking(len, connection);
        userRepository.close(connection);
        return users;
    }

    public User getProfileById(String id) throws SQLException {
        Connection connection = userRepository.openConnection();
        User user = userRepository.getProfileById(id, connection);
        userRepository.close(connection);
        return user;
    }

    public JSONArray getSolved(int frm, int len, int uCode) throws SQLException {
        Connection userConnection = userRepository.openConnection(), solvesConnection = solutionRepository.openConnection(), problemConnection = problemRepository.openConnection();
        JSONArray arr = solutionRepository.getSolved(frm, len, uCode, solvesConnection);
        JSONArray cpy = new JSONArray();
        arr.forEach((con)->{
            Problem problem;
            int pCode = ((JSONObject)con).getInt("code");
            try {
                problem = problemRepository.getProblemSimple(pCode, problemConnection);
                if(problem == null) return;
                JSONArray tags = new JSONArray(problem.getTags());
                tags.put(new JSONObject(Map.of(
                        "key", "diff",
                        "content", problem.getDifficultyCode()
                )));
                ((JSONObject)con).put("name", problem.getName());
                ((JSONObject)con).put("tags", tags);
                ((JSONObject)con).put("active", problem.isActive());
                cpy.put(con);
            } catch (SQLException e) {
                LOGGER.error("Fail to get problem data from solve history. pCode="+pCode+", uCode="+uCode, e);
            }
        });
        problemRepository.close(problemConnection);
        userRepository.close(userConnection);
        solutionRepository.close(solvesConnection);
        return cpy;
    }

    public void updateProfile(String name, String bio, int code) throws SQLException {
        Connection connection = userRepository.openConnectionForEdit();
        try {
            userRepository.updateProfile(name, bio, code, connection);
            userRepository.commitAndClose(connection);
        }
        catch (Exception e) {
            userRepository.rollbackAndClose(connection);
            LOGGER.error("Cannot update user profile", e);
            throw e;
        }
    }

    public void deleteUesr(int uCode) throws SQLException {
        Connection userConnection = userRepository.openConnectionForEdit(), solvesConnection = solutionRepository.openConnectionForEdit();
        try {
            userRepository.deleteUser(uCode, userConnection);
            solutionRepository.dropSolves(uCode, solvesConnection);
            userRepository.commitAndClose(userConnection);
            solutionRepository.commitAndClose(solvesConnection);
        }
        catch (Exception e) {
            userRepository.close(userConnection);
            solutionRepository.close(solvesConnection);
            LOGGER.error("Cannot delete user", e);
            throw  e;
        }
    }

    public boolean isUserStaredProblem(int uCode, int pCode) throws SQLException {
        Connection userConnection = userRepository.openConnection();
        try {
            LOGGER.debug("Query star of user "+uCode+" for "+pCode);
            boolean stared = userRepository.isUserStaredProblem(uCode, pCode, userConnection);
            userRepository.close(userConnection);
            return stared;
        }
        catch (Exception e) {
            LOGGER.error("Cannot check if user user stared: ", e);
            userRepository.close(userConnection);
            return false;
        }
    }

    public int changeUserStar(int uCode, int pCode) throws SQLException {
        int ret;
        Connection userConnection = userRepository.openConnectionForEdit();
        try {
            LOGGER.debug("Change star of user "+uCode+" for "+pCode);
            Vector<String> stared = getUserStaredList(uCode);
            if(userRepository.isUserStaredProblem(uCode, pCode, userConnection)) {
                stared.remove(Integer.toString(pCode));
                ret = 0;
            }
            else {
                stared.add(Integer.toString(pCode));
                ret = 1;
            }
            userRepository.updateStaredProblem(uCode, ","+String.join(",", stared)+",", userConnection);
            userRepository.commitAndClose(userConnection);
            return ret;
        }
        catch (Exception e) {
            LOGGER.error("Cannot check if user user stared: ", e);
            userRepository.rollbackAndClose(userConnection);
            throw e;
        }
    }

    public Vector<String> getUserStaredList(int uCode) throws SQLException {
        Connection userConnection = userRepository.openConnection();
        try {
            LOGGER.debug("Get user stared list from user "+uCode);
            String staredStr = userRepository.getUserStaredProblem(uCode, userConnection);
            String[] staredStrLst = staredStr.split(",");
            userRepository.close(userConnection);
            Vector<String> ret = new Vector<>();
            if(staredStrLst.length != 0) {
                Stream<String> arr = Arrays.stream(staredStrLst, 1, staredStrLst.length);
                arr.forEach(ret::add);
            }
            return ret;
        }
        catch (Exception e) {
            LOGGER.error("Cannot check of user stared: ", e);
            userRepository.close(userConnection);
            return new Vector<>();
        }
    }

    public JSONArray getUserStaredProblem(int uCode) throws SQLException {
        Connection userConnection = userRepository.openConnection(), problemConnection = problemRepository.openConnection();
        JSONArray cpy = new JSONArray();
        try {
            LOGGER.debug("Get user stared problem from user "+uCode);
            String staredStr = userRepository.getUserStaredProblem(uCode, userConnection);
            if(staredStr == null) return cpy;
            String[] staredStrLst = staredStr.split(",");
            for(String problemCode : staredStrLst) {
                if(problemCode.equals("")) continue;
                JSONObject con = new JSONObject();
                int pCode = Integer.parseInt(problemCode);
                try {
                    Problem problem = problemRepository.getProblemSimple(pCode, problemConnection);
                    JSONArray tags = new JSONArray(problem.getTags());
                    tags.put(new JSONObject(Map.of(
                            "key", "diff",
                            "content", problem.getDifficultyCode()
                    )));
                    con.put("code", pCode);
                    con.put("name", problem.getName());
                    con.put("tags", tags);
                    con.put("active", problem.isActive());
                    cpy.put(con);
                } catch (SQLException e) {
                    LOGGER.error("Fail to get problem data from user stared list. pCode="+pCode+". uCode="+uCode, e);
                }
            }
            problemRepository.close(problemConnection);
            userRepository.close(userConnection);
            return cpy;
        }
        catch (Exception e) {
            LOGGER.error("Cannot get problem that user stared. uCode="+uCode, e);
            userRepository.close(userConnection);
            return cpy;
        }
    }

    public void resetAccount(int code) throws SQLException {
        Connection userConnection = null;
        Connection solvesConnection = null;
        try {
            userConnection = userRepository.openConnectionForEdit();
            solvesConnection = solutionRepository.openConnectionForEdit();
            userRepository.updateExperience(code, 0, userConnection);
            solutionRepository.dropSolves(code, solvesConnection);
            userRepository.commitAndClose(userConnection);
            solutionRepository.commitAndClose(solvesConnection);
        } catch (SQLException e) {
            if (userConnection != null) {
                userRepository.rollbackAndClose(userConnection);
            }
            if (solvesConnection != null) {
                userRepository.rollbackAndClose(solvesConnection);
            }
            throw e;
        }
    }
}
