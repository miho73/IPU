package com.github.miho73.ipu.repositories;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;

@Repository("SolutionRepository")
public class SolutionRepository extends com.github.miho73.ipu.repositories.Repository {
    @Value("${db.identification.url}") private String DB_URL;
    @Value("${db.identification.username}") private String DB_USERNAME;
    @Value("${db.identification.password}") private String DB_PASSWORD;

    @Override
    @PostConstruct
    public void initRepository() {
        LOGGER.debug("Initializing SolutionRepository DB");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
    }

    public void addSolution(int userCode, int problemCode, short time_took, String judgeResult, short corrects, short total, int experience, Connection conn) throws SQLException {
        String sql = "INSERT INTO judges (user_code, problem_code, judge_time, time_took, judge_result, corrects, total, experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, userCode);
        psmt.setInt(2, problemCode);
        psmt.setTimestamp(3, timestamp);
        psmt.setShort(4, time_took);
        psmt.setString(5, judgeResult);
        psmt.setShort(6, corrects);
        psmt.setShort(7, corrects);
        psmt.setShort(8, total);
        psmt.setInt(9, experience);
    }

    public JSONArray getSolved(int frm, int len, int uCode, Connection conn) throws SQLException {
        String sql = "SELECT code, problem_code, solved_time, solving_time, correct " +
                     "FROM u"+uCode+" " +
                     "WHERE code<=((SELECT code FROM u"+uCode+" ORDER BY code DESC LIMIT 1)-?) " +
                     "ORDER BY code DESC LIMIT ?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, frm-1);
        psmt.setInt(2, len);
        ResultSet rs = psmt.executeQuery();

        JSONArray probs = new JSONArray();
        while (rs.next()) {
            JSONObject toPut = new JSONObject();
            toPut.put("code", rs.getInt("problem_code"));
            toPut.put("cor", rs.getBoolean("correct"));
            toPut.put("sol", rs.getString("solved_time"));
            toPut.put("solt", rs.getString("solving_time"));
            probs.put(toPut);
        }
        return probs;
    }

    public int getNumberOfSolves(int uCode, int pCode, Connection conn) throws SQLException {
        LOGGER.debug("Get Number of Solves request for user "+uCode+" pCode="+pCode);
        String sql = "SELECT COUNT(*) AS count FROM judges WHERE problem_code=? AND user_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, pCode);
        psmt.setInt(2, uCode);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return -1;
        return rs.getInt("count");
    }

    public void dropSolves(int uCode, Connection conn) throws SQLException {
        LOGGER.debug("Delete all judges of user code "+uCode);
        String sql = "DELETE FROM judges WHERE user_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, uCode);
        psmt.execute();
    }
}
