package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.domain.Problem;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.security.auth.kerberos.KerberosTicket;
import java.sql.*;

@Repository("SolutionRepository")
@PropertySources({
        @PropertySource("classpath:/properties/secret.properties"),
        @PropertySource("classpath:/properties/datasource.properties")
})
public class SolutionRepository {
    private DriverManagerDataSource dataSource;
    private Connection conn;

    private final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    @Value("${db.solution.url}") private String DB_URL;
    @Value("${db.solution.username}") private String DB_USERNAME;
    @Value("${db.solution.password}") private String DB_PASSWORD;

    @PostConstruct
    public void initProblemRepository() throws SQLException {
        LOGGER.debug("Initializing SolutionRepository DB");
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

    public void addUser(int usercode) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS u" + usercode +
                "(code SERIAL NOT NULL," +
                "problem_code INTEGER NOT NULL," +
                "solved_time TIMESTAMP WITH TIME ZONE NOT NULL," +
                "solving_time INTEGER NOT NULL," +
                "correct BOOLEAN NOT NULL" +
                ");";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.execute();
    }

    public void addSolution(int problemCode, int solveTime, boolean correct, int userCode) throws SQLException {
        String sql = "INSERT INTO u"+userCode+" (problem_code, solved_time, solving_time, correct) VALUES (?, ?, ?, ?)";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, problemCode);
        psmt.setTimestamp(2, timestamp);
        psmt.setInt(3, solveTime);
        psmt.setBoolean(4, correct);
        psmt.execute();
    }

    public JSONArray getSolved(int frm, int len, int uCode) throws SQLException {
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

    public int getNumberOfSolves(int uCode, int pCode) throws SQLException {
        LOGGER.debug("Get Number of Solves request for user "+uCode+" pCode="+pCode);
        String sql = "SELECT COUNT(*) AS count FROM u"+uCode+" WHERE problem_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, pCode);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return -1;
        return rs.getInt("count");
    }

    public void dropSolves(int uCode) throws SQLException {
        String sql = "DROP TABLE u"+uCode+";";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.execute();
    }
}
