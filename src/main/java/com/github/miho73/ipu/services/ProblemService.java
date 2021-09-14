package com.github.miho73.ipu.services;

import com.github.miho73.ipu.repositories.ProblemRepository;
import com.github.miho73.ipu.domain.Problem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service("ProblemService")
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final SessionService sessionService;

    @Autowired
    public ProblemService(ProblemRepository problemRepository, SessionService sessionService) {
        this.problemRepository = problemRepository;
        this.sessionService = sessionService;
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

    public void registerProblem(Problem problem, HttpSession session) throws SQLException {
        problemRepository.registerProblem(problem, sessionService.getName(session));
    }
}
