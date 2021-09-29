package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

@Repository("InviteRepository")
@PropertySources({
        @PropertySource("classpath:/properties/secret.properties"),
        @PropertySource("classpath:/properties/datasource.properties")
})
public class InviteRepository {
    private DriverManagerDataSource dataSource;
    private Connection conn;

    private final Logger LOGGER = LoggerFactory.getLogger(InviteRepository.class);

    @Value("${db.invite.url}") private String DB_URL;
    @Value("${db.invite.username}") private String DB_USERNAME;
    @Value("${db.invite.password}") private String DB_PASSWORD;

    @PostConstruct
    public void initInviteRepository() throws SQLException {
        LOGGER.debug("Initializing InviteRepository DB");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
        conn = dataSource.getConnection();
    }

    public boolean checkExist(String code) throws SQLException {
        String sql = "SELECT 1 FROM codes WHERE code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, code);
        ResultSet rs = psmt.executeQuery();

        return rs.next();
    }

    public List<String> getList() throws SQLException {
        String sql = "SELECT code FROM codes;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        ResultSet rs = psmt.executeQuery();

        List<String> code = new Vector<>();
        while (rs.next()) {
            code.add(rs.getString("code"));
        }
        return code;
    }

    public void insertCode(String nCode) throws SQLException {
        String sql = "INSERT INTO codes (code) VALUES (?);";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, nCode);
        psmt.execute();
    }
    public void updateCode(String prev, String nCode) throws SQLException {
        String sql = "UPDATE codes SET code=? WHERE code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, nCode);
        psmt.setString(2, prev);
        psmt.execute();
    }
    public void deleteCode(String prev) throws SQLException {
        String sql = "DELETE FROM codes WHERE code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, prev);
        psmt.execute();
    }
}
