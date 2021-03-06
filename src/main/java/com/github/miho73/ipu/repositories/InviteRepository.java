package com.github.miho73.ipu.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

@Repository("InviteRepository")
public class InviteRepository extends com.github.miho73.ipu.repositories.Repository {
    @Value("${db.invite.url}") private String DB_URL;
    @Value("${db.invite.username}") private String DB_USERNAME;
    @Value("${db.invite.password}") private String DB_PASSWORD;

    @Override
    @PostConstruct
    public void initRepository() {
        LOGGER.debug("Initializing InviteRepository DB");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
    }

    public boolean checkExist(String code, Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM codes WHERE code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, code);
        ResultSet rs = psmt.executeQuery();

        return rs.next();
    }

    public List<String> getList(Connection conn) throws SQLException {
        String sql = "SELECT code FROM codes;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        ResultSet rs = psmt.executeQuery();

        List<String> code = new Vector<>();
        while (rs.next()) {
            code.add(rs.getString("code"));
        }
        return code;
    }

    public void insertCode(String nCode, Connection conn) throws SQLException {
        String sql = "INSERT INTO codes (code) VALUES (?);";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, nCode);
        psmt.execute();
    }
    public void updateCode(String prev, String nCode, Connection conn) throws SQLException {
        String sql = "UPDATE codes SET code=? WHERE code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, nCode);
        psmt.setString(2, prev);
        psmt.execute();
    }
    public void deleteCode(String prev, Connection conn) throws SQLException {
        String sql = "DELETE FROM codes WHERE code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, prev);
        psmt.execute();
    }
}
