package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.domain.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

@Repository("UserRepository")
@PropertySources({
        @PropertySource("classpath:/properties/secret.properties"),
        @PropertySource("classpath:/properties/datasource.properties")
})
public class UserRepository {
    private DriverManagerDataSource dataSource;
    private Connection conn;

    private final Logger LOGGER = LoggerFactory.getLogger(UserRepository.class);

    @Value("${db.identification.url}") private String DB_URL;
    @Value("${db.identification.username}") private String DB_USERNAME;
    @Value("${db.identification.password}") private String DB_PASSWORD;

    @PostConstruct
    public void initUserRepository() throws SQLException {
        LOGGER.debug("Initializing UserRepository DB");
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

    public User getUserByCode(long code) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setLong(1, code);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return new User(
                rs.getLong("user_code"),
                rs.getString("user_id"),
                rs.getString("user_name"),
                rs.getString("privilege"),
                rs.getString("last_solve")
        );
    }

    public User getUserById(String id) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return new User(
                rs.getLong("user_code"),
                rs.getString("user_id"),
                rs.getString("user_name"),
                rs.getString("privilege"),
                rs.getString("last_solve")
        );
    }

    public User getProfileById(String id) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        User user = new User();
        user.setId(rs.getString("user_id"));
        user.setName(rs.getString("user_name"));
        user.setBio(rs.getString("bio"));
        user.setExperience(rs.getLong("experience"));
        return user;
    }

    // Those two methods return user object for general purpose
    public Object getUserDataById(String id, String column) throws SQLException {
        String sql = "SELECT "+column+" FROM iden WHERE user_id=?";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return rs.getObject(column);
    }
    public Object queryUserById(String id, String column) throws SQLException {
        String sql = "SELECT ? FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, column);
        psmt.setString(2, id);
        ResultSet rs = psmt.executeQuery();

        return rs.getObject(column);
    }

    // Add user data to identification database
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO iden" +
                "(user_id, user_name, user_password, user_salt, invite_code, bio, privilege, joined, experience) VALUES " +
                "(?, ?, ?, ?, ?, '', 'u', ? , 0);";

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, user.getId());
        psmt.setString(2, user.getName());
        psmt.setString(3, user.getPwd());
        psmt.setString(4, user.getSalt());
        psmt.setString(5, user.getInviteCode());
        psmt.setTimestamp(6, timestamp);
        psmt.execute();
    }

    // Update user data type of string
    public void updateUserStringById(String id, String column, String newValue) throws SQLException {
        String sql = "UPDATE iden SET "+column+"=? WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, newValue);
        psmt.setString(2, id);
        psmt.execute();
    }

    // Update user data type of timestamp
    public void updateUserTSById(String id, String column, Timestamp timestamp) throws SQLException {
        String sql = "UPDATE iden SET "+column+"=? WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setTimestamp(1, timestamp);
        psmt.setString(2, id);
        psmt.execute();
    }

    // Get user data required to do login
    public User getUserForAuthentication(String id) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return new User(rs.getLong("user_code"), rs.getString("user_id"), rs.getString("user_password"), rs.getString("user_salt"));
    }

    public List<User> getUserRanking(long len) throws SQLException {
        String sql = "SELECT user_id, user_name, bio, experience FROM iden ORDER BY experience DESC LIMIT ?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setLong(1, len);
        ResultSet rs = psmt.executeQuery();

        List<User> ret = new Vector<>();
        while(rs.next()) {
            User user = new User();
            user.setId(rs.getString("user_id"));
            user.setName(rs.getString("user_name"));
            user.setBio(rs.getString("bio"));
            user.setExperience(rs.getLong("experience"));
            ret.add(user);
        }
        return ret;
    }

    public void addExperience(long exp, long userCode) throws SQLException {
        LOGGER.debug("Add exp to "+userCode+" amount of "+exp);
        String sql = "UPDATE iden SET experience=((SELECT experience FROM iden WHERE user_code=?)+?) WHERE user_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setLong(1, userCode);
        psmt.setLong(2, exp);
        psmt.setLong(3, userCode);
        psmt.execute();
    }
}
