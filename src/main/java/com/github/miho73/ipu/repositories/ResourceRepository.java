package com.github.miho73.ipu.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;

@Repository("ResourceRepository")
@PropertySources({
        @PropertySource("classpath:/properties/secret.properties"),
        @PropertySource("classpath:/properties/datasource.properties")
})
public class ResourceRepository {
    private DriverManagerDataSource dataSource;
    private Connection conn;

    private final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    @Value("${db.problem.url}") private String DB_URL;
    @Value("${db.problem.username}") private String DB_USERNAME;
    @Value("${db.problem.password}") private String DB_PASSWORD;

    @PostConstruct
    public void initResourceRepository() throws SQLException {
        LOGGER.debug("Initializing ResourceRepository DB");
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

    public void addResource(byte[] resource, String hash, String adder) throws SQLException {
        LOGGER.debug("Add resource to DB. HASH="+hash+". Added_by="+adder);
        String sql = "INSERT INTO resources (resource_code, resource, registered, registered_by) VALUES (?, ?, ?, ?);";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, hash);
        psmt.setBytes(2, resource);
        psmt.setTimestamp(3, timestamp);
        psmt.setString(4, adder);
        psmt.execute();
    }

    public byte[] getResource(String hash) throws SQLException {
        LOGGER.debug("Get resource hash of "+hash);
        String sql = "SELECT resource FROM resources WHERE resource_code=?";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, hash);
        ResultSet rs = psmt.executeQuery();
        if(!rs.next()) return null;
        return rs.getBytes("resource");
    }
}
