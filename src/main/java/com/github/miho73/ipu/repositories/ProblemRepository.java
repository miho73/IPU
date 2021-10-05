package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.domain.Problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.List;
import java.util.Vector;

@Repository("ProblemRepository")
public class ProblemRepository {
    private DriverManagerDataSource dataSource;
    private Connection conn;

    private final Logger LOGGER = LoggerFactory.getLogger(ProblemRepository.class);

    @Value("${db.problem.url}") private String DB_URL;
    @Value("${db.problem.username}") private String DB_USERNAME;
    @Value("${db.problem.password}") private String DB_PASSWORD;

    @PostConstruct
    public void initProblemRepository() throws SQLException {
        LOGGER.debug("Initializing ProblemRepository DB");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
        conn = dataSource.getConnection();
    }

    public void close() throws SQLException {
        conn.close();
    }

    public Problem getProblemSimple(int pCode) throws SQLException {
        String sql = "SELECT problem_name, problem_category, problem_difficulty, tags FROM prob WHERE problem_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        psmt.setInt(1, pCode);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;

        Problem ret = new Problem();
        ret.setName(rs.getString("problem_name"));
        ret.setCategory(rs.getString("problem_category"));
        ret.setDifficulty(rs.getString("problem_difficulty"));
        ret.setTags(rs.getString("tags"));
        return ret;
    }

    public List<Problem> getProblemBriefly(int from, int len) throws SQLException {
        String sql = "SELECT problem_code, problem_name, problem_category, problem_difficulty, tags FROM prob WHERE problem_code>=? ORDER BY problem_code LIMIT ?;";
        PreparedStatement psmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        psmt.setInt(1, from);
        psmt.setInt(2, len);
        ResultSet rs = psmt.executeQuery();

        List<Problem> pList = new Vector<>();
        while (rs.next()) {
            Problem problem = new Problem();
            problem.setCode(rs.getInt("problem_code"));
            problem.setName(rs.getString("problem_name"));
            problem.setCategory(rs.getString("problem_category"));
            problem.setDifficulty(rs.getString("problem_difficulty"));
            problem.setTags(rs.getString("tags"));
            pList.add(problem);
        }
        return pList;
    }

    public Problem getProblem(int code) throws SQLException {
        String sql = "SELECT problem_code, problem_name, problem_content, problem_solution, problem_answer, problem_hint, extr_tabs, has_hint FROM prob WHERE problem_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        psmt.setInt(1, code);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;

        Problem problem = new Problem();
        problem.setCode(rs.getInt("problem_code"));
        problem.setName(rs.getString("problem_name"));
        problem.setContent(rs.getString("problem_content"));
        problem.setSolution(rs.getString("problem_solution"));
        problem.setAnswer(rs.getString("problem_answer"));
        problem.setHint(rs.getString("problem_hint"));
        problem.setExternalTabs(rs.getString("extr_tabs"));
        problem.setHasHint(rs.getBoolean("has_hint"));
        return problem;
    }

    public Problem getFullProblem(int code) throws SQLException {
        String sql = "SELECT * FROM prob WHERE problem_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        psmt.setInt(1, code);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;

        Problem problem = new Problem();
        problem.setCode(rs.getInt("problem_code"));
        problem.setName(rs.getString("problem_name"));
        problem.setDifficulty(rs.getString("problem_difficulty"));
        problem.setCategory(rs.getString("problem_category"));
        problem.setContent(rs.getString("problem_content"));
        problem.setSolution(rs.getString("problem_solution"));
        problem.setAnswer(rs.getString("problem_answer"));
        problem.setHint(rs.getString("problem_hint"));
        problem.setExternalTabs(rs.getString("extr_tabs"));
        problem.setTags(rs.getString("tags"));
        problem.setHasHint(rs.getBoolean("has_hint"));
        return problem;
    }

    public void registerProblem(Problem problem, String auther) throws SQLException {
        String sql = "INSERT INTO prob" +
                "(problem_name, problem_category, problem_difficulty, problem_content, problem_solution, problem_answer, problem_hint, author_name, added_at, last_modified, extr_tabs, has_hint, tags) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, problem.getName());
        psmt.setString(2, problem.getCategoryCode());
        psmt.setString(3, problem.getDifficultyCode());
        psmt.setString(4, problem.getContent());
        psmt.setString(5, problem.getSolution());
        psmt.setString(6, "");
        psmt.setString(7, "");
        psmt.setString(8, auther);
        psmt.setTimestamp(9, timestamp);
        psmt.setTimestamp(10, timestamp);
        psmt.setString(11, "");
        psmt.setBoolean(12, false);
        psmt.setString(13, problem.getTags());

        psmt.execute();
    }
    public void updateProblem(Problem problem) throws SQLException {
        String sql = "UPDATE prob SET " +
                "problem_name=?,"+
                "problem_category=?,"+
                "problem_difficulty=?,"+
                "problem_content=?,"+
                "problem_solution=?,"+
                "problem_answer=?,"+
                "problem_hint=?,"+
                "last_modified=?," +
                "extr_tabs=?," +
                "has_hint=?," +
                "tags=?" +
                " WHERE problem_code=?;";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, problem.getName());
        psmt.setString(2, problem.getCategoryCode());
        psmt.setString(3, problem.getDifficultyCode());
        psmt.setString(4, problem.getContent());
        psmt.setString(5, problem.getSolution());
        psmt.setString(6, problem.getAnswer());
        psmt.setString(7, problem.getHint());
        psmt.setTimestamp(8, timestamp);
        psmt.setString(9, problem.getExternalTabs());
        psmt.setBoolean(10, problem.isHasHint());
        psmt.setString(11, problem.getTags());
        psmt.setInt(12, problem.getCode());

        psmt.execute();
    }
}
