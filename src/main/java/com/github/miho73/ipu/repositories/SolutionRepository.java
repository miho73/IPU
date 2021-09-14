package com.github.miho73.ipu.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

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

    public void addUser(long usercode) throws SQLException {
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

    public void addSolution(long problemCode, long solveTime, boolean correct, long userCode) throws SQLException {
        String sql = "INSERT INTO u"+userCode+" (problem_code, solved_time, solving_time, correct) VALUES (?, ?, ?, ?)";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setLong(1, problemCode);
        psmt.setTimestamp(2, timestamp);
        psmt.setLong(3, solveTime);
        psmt.setBoolean(4, correct);
        psmt.execute();
    }
}
