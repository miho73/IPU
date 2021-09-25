package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.domain.User;
import com.github.miho73.ipu.repositories.ProblemRepository;
import com.github.miho73.ipu.repositories.SolutionRepository;
import com.github.miho73.ipu.repositories.UserRepository;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service("UserService")
public class UserService {
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SolutionRepository solutionRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    @Autowired
    public UserService(UserRepository userRepository, SolutionRepository solutionRepository, ProblemRepository problemRepository) {
        this.userRepository = userRepository;
        this.solutionRepository = solutionRepository;
        this.problemRepository = problemRepository;
    }

    public User getUserByCode(int code) throws SQLException {
        return userRepository.getUserByCode(code);
    }
    public User getUserById(String id) throws SQLException {
        return userRepository.getUserById(id);
    }
    public Object getUserDataById(String id, String column) throws SQLException {
        return userRepository.getUserDataById(id, column);
    }
    public void updateStringById(String id, String column, String nData) throws SQLException {
        userRepository.updateUserStringById(id, column, nData);
    }

    public String getUserRanking(int len) throws SQLException {
        List<User> users =  userRepository.getUserRanking(len);
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
        return userRepository.getProfileById(id);
    }

    public String getSolved(int frm, int len, String id) throws SQLException {
        int uCode = (int) userRepository.getUserDataById(id, "user_code");
        JSONArray arr = solutionRepository.getSolved(frm, len, uCode);
        JSONArray cpy = new JSONArray();
        arr.forEach((con)->{
            Problem problem = null;
            int pCode = ((JSONObject)con).getInt("code");
            try {
                problem = problemRepository.getProblemSimple(pCode);
            } catch (SQLException e) {
                LOGGER.debug("Fail to get problem data from solve history. pCode="+pCode);
            }
            assert problem != null;
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
        });
        return cpy.toString();
    }

    public void updateProfile(String name, String bio, int code) throws SQLException {
        userRepository.updateProfile(name, bio, code);
    }

    public void deleteUesr(int uCode) throws SQLException {
        userRepository.deleteUser(uCode);
        solutionRepository.dropSolves(uCode);
    }
}
