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

@Repository("InviteRepository")
@PropertySources({
        @PropertySource("classpath:/properties/secret.properties"),
        @PropertySource("classpath:/properties/datasource.properties")
})
public class InviteRepository {
    private DriverManagerDataSource dataSource;
    private Connection conn;

    private final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

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

    public void runNonQuery() {

    }
}
