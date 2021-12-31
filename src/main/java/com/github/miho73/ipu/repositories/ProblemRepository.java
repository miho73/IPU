package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.domain.Problem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Vector;

@Repository("ProblemRepository")
public class ProblemRepository extends com.github.miho73.ipu.repositories.Repository {
    @Value("${db.problem.url}") private String DB_URL;
    @Value("${db.problem.username}") private String DB_USERNAME;
    @Value("${db.problem.password}") private String DB_PASSWORD;

    @Override
    @PostConstruct
    public void initRepository() {
        LOGGER.debug("Initializing ProblemRepository DB");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
    }

    public Problem getProblemSimple(int pCode, Connection conn) throws SQLException {
        String sql = "SELECT problem_name, problem_category, problem_difficulty, tags, active FROM prob WHERE problem_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, pCode);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;

        Problem ret = new Problem();
        ret.setName(rs.getString("problem_name"));
        ret.setCategory(rs.getString("problem_category"));
        ret.setDifficulty(rs.getString("problem_difficulty"));
        ret.setTags(rs.getString("tags"));
        ret.setActive(rs.getBoolean("active"));
        return ret;
    }

    public List<Problem> getProblemBriefly(int from, int len, Connection conn) throws SQLException {
        String sql = "SELECT problem_code, problem_name, problem_category, problem_difficulty, tags, active FROM prob WHERE problem_code>=? ORDER BY problem_code LIMIT ?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
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
            problem.setActive(rs.getBoolean("active"));
            pList.add(problem);
        }
        return pList;
    }
    public List<Problem> searchProblem(int from, int len, Map<String, String> hKV, String where, Connection conn) throws SQLException {
        String sql = "SELECT problem_code, problem_name, problem_category, problem_difficulty, tags, active FROM prob WHERE problem_code>=? "+where+" ORDER BY problem_code LIMIT ?;";
        LOGGER.debug("query="+where);
        PreparedStatement psmt = conn.prepareStatement(sql);
        int currentFilling = 2;
        psmt.setInt(1, from);
        if(hKV.containsKey("has")) {
            String fi = "%"+hKV.get("has")+"%";
            psmt.setString(2, fi);
            psmt.setString(3, fi);
            currentFilling = 4;
        }
        if(hKV.containsKey("cat")) {
            String fi = hKV.get("cat");
            psmt.setString(currentFilling, fi);
            currentFilling++;
        }
        if(hKV.containsKey("dif")) {
            String fi = hKV.get("dif");
            psmt.setString(currentFilling, fi);
            currentFilling++;
        }
        psmt.setInt(currentFilling, len);
        ResultSet rs = psmt.executeQuery();

        List<Problem> pList = new Vector<>();
        while (rs.next()) {
            Problem problem = new Problem();
            problem.setCode(rs.getInt("problem_code"));
            problem.setName(rs.getString("problem_name"));
            problem.setCategory(rs.getString("problem_category"));
            problem.setDifficulty(rs.getString("problem_difficulty"));
            problem.setTags(rs.getString("tags"));
            problem.setActive(rs.getBoolean("active"));
            pList.add(problem);
        }
        return pList;
    }

    public Problem getProblem(int code, Connection conn) throws SQLException {
        String sql = "SELECT problem_code, problem_name, problem_content, problem_solution, problem_difficulty, tags, active FROM prob WHERE problem_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, code);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;

        Problem problem = new Problem();
        problem.setCode(rs.getInt("problem_code"));
        problem.setName(rs.getString("problem_name"));
        problem.setContent(rs.getString("problem_content"));
        problem.setSolution(rs.getString("problem_solution"));
        problem.setDifficulty(rs.getString("problem_difficulty"));
        problem.setTags(rs.getString("tags"));
        problem.setActive(rs.getBoolean("active"));
        return problem;
    }

    public Problem getFullProblem(int code, Connection conn) throws SQLException {
        String sql = "SELECT * FROM prob WHERE problem_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
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
        problem.setTags(rs.getString("tags"));
        problem.setAuthor_name(rs.getString("author_name"));
        problem.setAdded_at(rs.getTimestamp("added_at"));
        problem.setLast_modified((rs.getTimestamp("last_modified")));
        problem.setActive(rs.getBoolean("active"));
        return problem;
    }

    public void registerProblem(Problem problem, Connection conn) throws SQLException {
        String sql = "INSERT INTO prob" +
                "(problem_name, problem_category, problem_difficulty, problem_content, problem_solution, author_name, added_at, last_modified, tags, active) VALUES " +
                "(?, ?, ?, ?, ?, ?, ?, ?, ?, true);";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, problem.getName());
        psmt.setString(2, problem.getCategoryCode());
        psmt.setString(3, problem.getDifficultyCode());
        psmt.setString(4, problem.getContent());
        psmt.setString(5, problem.getSolution());
        psmt.setString(6, problem.getAuthor_name());
        psmt.setTimestamp(7, timestamp);
        psmt.setTimestamp(8, timestamp);
        psmt.setString(9, problem.getTags());

        psmt.execute();
    }
    public void updateProblem(Problem problem, Connection conn) throws SQLException {
        String sql = "UPDATE prob SET " +
                "problem_name=?,"+
                "problem_category=?,"+
                "problem_difficulty=?,"+
                "problem_content=?,"+
                "problem_solution=?,"+
                "last_modified=?," +
                "tags=?," +
                "active=?" +
                " WHERE problem_code=?;";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, problem.getName());
        psmt.setString(2, problem.getCategoryCode());
        psmt.setString(3, problem.getDifficultyCode());
        psmt.setString(4, problem.getContent());
        psmt.setString(5, problem.getSolution());
        psmt.setTimestamp(6, timestamp);
        psmt.setString(7, problem.getTags());
        psmt.setBoolean(8, problem.isActive());
        psmt.setInt(9, problem.getCode());

        psmt.execute();
    }

    public int getNumberOfProblemsInCategory(Problem.PROBLEM_CATEGORY category, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM prob WHERE problem_category=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, category.toString().substring(0, 4).toLowerCase());
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return 0;

        return rs.getInt("cnt");
    }

    public int getNumberOfProblems(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM prob;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return -1;

        return rs.getInt("cnt");
    }

    public Vector<Problem> searchProblemUsingResource(String code, Connection conn) throws SQLException {
        String sql = "SELECT problem_code, problem_name FROM prob WHERE (problem_content LIKE ? OR problem_solution LIKE ?);";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, "%"+code+"%");
        psmt.setString(2, "%"+code+"%");
        ResultSet rs = psmt.executeQuery();

        Vector<Problem> ret = new Vector<>();
        while(rs.next()) {
            Problem problem = new Problem();
            problem.setCode(rs.getInt("problem_code"));
            problem.setName(rs.getString("problem_name"));
            ret.add(problem);
        }
        return ret;
    }
}