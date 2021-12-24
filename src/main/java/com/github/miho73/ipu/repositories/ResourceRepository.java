package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.domain.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.Vector;

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

    public boolean isResourceExists(String hash, Connection conn) throws SQLException {
        LOGGER.debug("Search resource hash of "+hash);
        String sql = "SELECT resource_code FROM resources WHERE resource_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, hash);
        ResultSet rs = psmt.executeQuery();
        return rs.next();
    }

    public Resource queryResource(String hash, Connection conn) throws SQLException {
        LOGGER.debug("Query resource hash of "+hash);
        String sql = "SELECT * FROM resources WHERE resource_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, hash);
        ResultSet rs = psmt.executeQuery();
        Resource resource = new Resource();
        if(!rs.next()) return null;
        resource.setResource_code(rs.getString("resource_code"));
        resource.setResource_name(rs.getString("resource_name"));
        resource.setRegistered_by(rs.getString("registered_by"));
        resource.setRegistered(rs.getTimestamp("registered"));
        return resource;
    }
    public Resource queryResourceByName(String name, Connection conn) throws SQLException {
        LOGGER.debug("Query resource name of "+name);
        String sql = "SELECT * FROM resources WHERE resource_name=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, name);
        ResultSet rs = psmt.executeQuery();
        Resource resource = new Resource();
        if(!rs.next()) return null;
        resource.setResource_code(rs.getString("resource_code"));
        resource.setResource_name(rs.getString("resource_name"));
        resource.setRegistered_by(rs.getString("registered_by"));
        resource.setRegistered(rs.getTimestamp("registered"));
        return resource;
    }

    public Vector<Resource> getAllResources(Connection conn) throws SQLException {
        LOGGER.debug("Query of all resources");
        String sql = "SELECT * FROM resources;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        ResultSet rs = psmt.executeQuery();
        Vector<Resource> resources = new Vector<>();
        while (rs.next()) {
            Resource resource = new Resource();
            resource.setResource_code(rs.getString("resource_code"));
            resource.setResource_name(rs.getString("resource_name"));
            resource.setRegistered_by(rs.getString("registered_by"));
            resource.setRegistered(rs.getTimestamp("registered"));
            resources.add(resource);
        }
        return resources;
    }

    public void updateName(String hash, String name, Connection conn) throws SQLException {
        LOGGER.debug("Change name of resource '"+hash+"' to '"+name+"'");
        String sql = "UPDATE resources SET resource_name=? WHERE resource_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, name);
        psmt.setString(2, hash);
        psmt.execute();
    }

    public void deleteResource(String code, Connection conn) throws SQLException {
        LOGGER.debug("Delete resource '"+code+"'");
        String sql = "DELETE FROM resources WHERE resource_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, code);
        psmt.execute();
    }
}
