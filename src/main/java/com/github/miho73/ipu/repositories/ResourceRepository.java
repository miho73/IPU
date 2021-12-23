package com.github.miho73.ipu.repositories;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;

@Repository("ResourceRepository")
public class ResourceRepository extends com.github.miho73.ipu.repositories.Repository {
    @Value("${db.problem.url}") private String DB_URL;
    @Value("${db.problem.username}") private String DB_USERNAME;
    @Value("${db.problem.password}") private String DB_PASSWORD;

    @Override
    @PostConstruct
    public void initRepository() {
        LOGGER.debug("Initializing ResourceRepository DB");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
    }

    public void addResource(byte[] resource, String hash, String adder, Connection conn) throws SQLException {
        LOGGER.debug("Add resource to DB. HASH="+hash+". Added_by="+adder);
        String sql = "INSERT INTO resources (resource_code, resource, registered, registered_by, resource_name) VALUES (?, ?, ?, ?, '');";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, hash);
        psmt.setBytes(2, resource);
        psmt.setTimestamp(3, timestamp);
        psmt.setString(4, adder);
        psmt.execute();
    }

    public byte[] getResource(String hash, Connection conn) throws SQLException {
        LOGGER.debug("Get resource hash of "+hash);
        String sql = "SELECT resource FROM resources WHERE resource_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, hash);
        ResultSet rs = psmt.executeQuery();
        if(!rs.next()) return null;
        return rs.getBytes("resource");
    }
}
