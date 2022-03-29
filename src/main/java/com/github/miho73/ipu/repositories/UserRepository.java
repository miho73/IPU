package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.List;
import java.util.Vector;

@Repository("UserRepository")
public class UserRepository extends com.github.miho73.ipu.repositories.Repository {
    @Value("${db.identification.url}") private String DB_URL;
    @Value("${db.identification.username}") private String DB_USERNAME;
    @Value("${db.identification.password}") private String DB_PASSWORD;

    @Override
    @PostConstruct
    public void initRepository() {
        LOGGER.debug("Initializing UserRepository DB");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
    }

    public User getUserByCode(int code, Connection conn) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, code);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        User user = new User();
        user.setCode(rs.getInt("user_code"));
        user.setId(rs.getString("user_id"));
        user.setName(rs.getString("user_name"));
        user.setEmail(rs.getString("email"));
        user.setExperience(rs.getInt("experience"));
        user.setBio(rs.getString("bio"));
        user.setJoined(rs.getTimestamp("joined"));
        user.setLastLogin(rs.getTimestamp("last_login"));
        user.setLastSolve(rs.getTimestamp("last_solve"));
        return user;
    }

    public User getUserById(String id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        User user = new User();
        user.setCode(rs.getInt("user_code"));
        user.setId(rs.getString("user_id"));
        user.setName(rs.getString("user_name"));
        user.setPrivilege(rs.getString("privilege"));
        return user;
    }

    public User getProfileById(String id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        User user = new User();
        user.setCode(rs.getInt("user_code"));
        user.setId(rs.getString("user_id"));
        user.setName(rs.getString("user_name"));
        user.setBio(rs.getString("bio"));
        user.setExperience(rs.getInt("experience"));
        return user;
    }

    public Object getUserDataById(String id, String column, Connection conn) throws SQLException {
        String sql = "SELECT "+column+" FROM iden WHERE user_id=?";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return rs.getObject(column);
    }
    public Object queryUserById(String id, String column, Connection conn) throws SQLException {
        String sql = "SELECT ? FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, column);
        psmt.setString(2, id);
        ResultSet rs = psmt.executeQuery();

        return rs.getObject(column);
    }
    public Object getUserDataByCode(int code, String column, Connection conn) throws SQLException {
        String sql = "SELECT "+column+" FROM iden WHERE user_code=?";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, code);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        return rs.getObject(column);
    }
    public Object queryUserByCode(int code, String column, Connection conn) throws SQLException {
        String sql = "SELECT ? FROM iden WHERE user_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, column);
        psmt.setInt(2, code);
        ResultSet rs = psmt.executeQuery();

        return rs.getObject(column);
    }

    public void addUser(User user, Connection conn) throws SQLException {
        String sql = "INSERT INTO iden" +
                "(user_id, user_name, user_password, user_salt, invite_code, bio, privilege, joined, experience, stared_problem) VALUES " +
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
    public void updateUserStringById(String id, String column, String newValue, Connection conn) throws SQLException {
        String sql = "UPDATE iden SET "+column+"=? WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, newValue);
        psmt.setString(2, id);
        psmt.execute();
    }

    // Update user data type of timestamp
    public void updateUserTSById(String id, String column, Timestamp timestamp, Connection conn) throws SQLException {
        String sql = "UPDATE iden SET "+column+"=? WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setTimestamp(1, timestamp);
        psmt.setString(2, id);
        psmt.execute();
    }
    public void updateUserTSByCode(int code, String column, Timestamp timestamp, Connection conn) throws SQLException {
        String sql = "UPDATE iden SET "+column+"=? WHERE user_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setTimestamp(1, timestamp);
        psmt.setInt(2, code);
        psmt.execute();
    }

    // Get user data required to do login
    public User getUserForAuthentication(String id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM iden WHERE user_id=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, id);
        ResultSet rs = psmt.executeQuery();

        if(!rs.next()) return null;
        User user = new User();
        user.setCode(rs.getInt("user_code"));
        user.setId(rs.getString("user_id"));
        user.setPwd(rs.getString("user_password"));
        user.setSalt(rs.getString("user_salt"));
        user.setPrivilege(rs.getString("privilege"));
        return user;
    }

    public List<User> getUserRanking(int len, Connection conn) throws SQLException {
        String sql = "SELECT user_id, user_name, bio, experience FROM iden ORDER BY experience DESC LIMIT ?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, len);
        ResultSet rs = psmt.executeQuery();

        List<User> ret = new Vector<>();
        while(rs.next()) {
            User user = new User();
            user.setId(rs.getString("user_id"));
            user.setName(rs.getString("user_name"));
            user.setBio(rs.getString("bio"));
            user.setExperience(rs.getInt("experience"));
            ret.add(user);
        }
        return ret;
    }

    public void addExperience(int exp, int userCode, Connection conn) throws SQLException {
        String sql = "UPDATE iden SET experience=((SELECT experience FROM iden WHERE user_code=?)+?) WHERE user_code=?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, userCode);
        psmt.setInt(2, exp);
        psmt.setInt(3, userCode);
        psmt.execute();
    }

    public void updateProfile(String name, String bio, int code, Connection conn) throws SQLException {
        String sql = "UPDATE iden SET user_name=?, bio=? WHERE user_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, name);
        psmt.setString(2, bio);
        psmt.setInt(3, code);
        psmt.execute();
    }

    public void updatePassword(String nPwd, String nSalt, int code, Connection conn) throws SQLException {
        String sql = "UPDATE iden SET user_password=?, user_salt=? WHERE user_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, nPwd);
        psmt.setString(2, nSalt);
        psmt.setInt(3, code);
        psmt.execute();
    }

    public void deleteUser(int uCode, Connection conn) throws SQLException {
        String sql = "DELETE FROM iden WHERE user_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, uCode);
        psmt.execute();
    }

    public boolean isUserStaredProblem(int uCode, int pCode, Connection conn) throws SQLException {
        String sql = "SELECT stared_problem FROM iden WHERE stared_problem LIKE ? AND user_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, "%,"+pCode+",%");
        psmt.setInt(2, uCode);
        ResultSet rs = psmt.executeQuery();

        return rs.next();
    }
    public String getUserStaredProblem(int uCode, Connection conn) throws SQLException {
        String sql = "SELECT stared_problem FROM iden WHERE user_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, uCode);
        ResultSet rs = psmt.executeQuery();
        if(!rs.next()) throw null;
        return rs.getString("stared_problem");
    }

    public void updateStaredProblem(int uCode, String newList, Connection conn) throws SQLException {
        String sql = "UPDATE iden SET stared_problem=? WHERE user_code=?;";

        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setString(1, newList);
        psmt.setInt(2, uCode);
        psmt.execute();
    }
}
