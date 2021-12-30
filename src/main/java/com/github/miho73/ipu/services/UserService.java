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
import java.util.List;
import java.util.Map;

@Service("UserService")
public class UserService {
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SolutionRepository solutionRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public UserService(UserRepository userRepository, SolutionRepository solutionRepository, ProblemRepository problemRepository) {
        this.userRepository = userRepository;
        this.solutionRepository = solutionRepository;
        this.problemRepository = problemRepository;
    }

    public User getUserByCode(int code) throws SQLException {
        Connection connection = userRepository.openConnection();
        User user = userRepository.getUserByCode(code, connection);
        userRepository.close(connection);
        return  user;
    }
    public User getUserById(String id) throws SQLException {
        Connection connection = userRepository.openConnection();
        User user = userRepository.getUserById(id, connection);
        userRepository.close(connection);
        return user;
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

    public String getUserRanking(int len) throws SQLException {
        Connection connection = userRepository.openConnection();
        List<User> users =  userRepository.getUserRanking(len, connection);
        userRepository.close(connection);
        JSONArray uLst = new JSONArray();
        users.forEach((user)->{
            JSONObject usr = new JSONObject();
            usr.put("uname", user.getName());
            usr.put("bio", user.getBio());
            usr.put("exp", user.getExperience());
            usr.put("id", user.getId());
            uLst.put(usr);
        });
        return uLst.toString();
    }

    public User getProfileById(String id) throws SQLException {
        Connection connection = userRepository.openConnection();
        User user = userRepository.getProfileById(id, connection);
        userRepository.close(connection);
        return user;
    }

    public String getSolved(int frm, int len, String id) throws SQLException {
        Connection userConnection = userRepository.openConnection(), solvesConnection = solutionRepository.openConnection(), problemConnection = problemRepository.openConnection();
        int uCode = (int) userRepository.getUserDataById(id, "user_code", userConnection);
        JSONArray arr = solutionRepository.getSolved(frm, len, uCode, solvesConnection);
        JSONArray cpy = new JSONArray();
        arr.forEach((con)->{
            Problem problem;
            int pCode = ((JSONObject)con).getInt("code");
            try {
                problem = problemRepository.getProblemSimple(pCode, problemConnection);
                JSONArray tags = new JSONArray(problem.getTags());
                tags.put(new JSONObject(Map.of(
                        "key", "cate",
                        "content", problem.getCategoryCode()
                )));
                tags.put(new JSONObject(Map.of(
                        "key", "diff",
                        "content", problem.getDifficultyCode()
                )));
                ((JSONObject)con).put("name", problem.getName());
                ((JSONObject)con).put("tags", tags);
                cpy.put(con);
            } catch (SQLException e) {
                LOGGER.error("Fail to get problem data from solve history. pCode="+pCode, e);
            }
        });
        problemConnection.close();
        userRepository.close(userConnection);
        solutionRepository.close(solvesConnection);
        return cpy.toString();
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
}
