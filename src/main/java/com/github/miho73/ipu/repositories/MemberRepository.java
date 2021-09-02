package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.domain.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository("MemberRepository")
@PropertySources({
        @PropertySource("classpath:/properties/secret.properties"),
        @PropertySource("classpath:/properties/datasource.properties")
})
public class MemberRepository {
    private DriverManagerDataSource dataSource;
    private Connection conn;

    private final Logger LOGGER = LoggerFactory.getLogger(MemberRepository.class);

    @Value("${db.url}") private String DB_URL;
    @Value("${db.username}") private String DB_USERNAME;
    @Value("${db.password}") private String DB_PASSWORD;

    @PostConstruct
    public void initMemberRepository() {
        LOGGER.debug("Initializing MemberRepository");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
    }

    public void open() throws SQLException {
        conn = dataSource.getConnection();
    }
    public void close() throws SQLException {
        conn.close();
    }

    public Member getUserByCode(long code) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setLong(1, code);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return new Member(rs.getLong("user_code"), rs.getString("user_id"), rs.getString("user_name"));
    }
    public Member getUserById(String id) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return new Member(rs.getLong("user_code"), rs.getString("user_id"), rs.getString("user_name"));
    }
    public Member getUserForAuthentication(String id) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return new Member(rs.getLong("user_code"), rs.getString("user_id"), rs.getString("user_password"), rs.getString("user_salt"));
    }
}
