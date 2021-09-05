package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.Problem;
import com.github.miho73.ipu.repositories.ProblemRepository;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service("ProblemService")
public class ProblemService {
    private final ProblemRepository problemRepository;

    @Autowired
    public ProblemService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public JSONArray getProblemList(int from, int length) throws SQLException {
        JSONArray root = new JSONArray();
        List<Problem> problems = problemRepository.getProblemBriefly(from, length);
        for(Problem problem : problems) {
            JSONObject element = new JSONObject();
            element.put("code", problem.getCode());
            element.put("name", problem.getName());
            JSONArray tags = new JSONArray(problem.getTags());
            tags.put(new JSONObject(Map.of("key", "diff", "content", problem.getDifficulty())));
            tags.put(new JSONObject(Map.of("key", "cate", "content", problem.getCategory())));
            element.put("tags", tags);
            root.put(element);
        }
        return root;
    }

    public Problem getProblem(int code) throws SQLException {
        return problemRepository.getProblem(code);
    }
}
