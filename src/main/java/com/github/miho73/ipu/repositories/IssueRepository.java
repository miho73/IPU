package com.github.miho73.ipu.repositories;

import com.github.miho73.ipu.domain.Issue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.List;
import java.util.Vector;

@Repository("IssueRepository")
public class IssueRepository extends com.github.miho73.ipu.repositories.Repository {
    @Value("${db.problem.url}") private String DB_URL;
    @Value("${db.problem.username}") private String DB_USERNAME;
    @Value("${db.problem.password}") private String DB_PASSWORD;

    private final String ISSUE_CODE = "issue_code";
    private final String ISSUE_NANE = "issue_name";
    private final String ISSUE_CONTENT = "issue_content";
    private final String VOTE = "vote";
    private final String FOR_PROBLEM = "for_problem";
    private final String ISSUE_TYPE = "issue_type";
    private final String AUTHOR = "author";
    private final String WRITTEN_AT = "written_at";
    private final String ISSUE_STATUS = "status";

    @Override
    @PostConstruct
    public void initRepository() {
        LOGGER.debug("Initializing IssueRepository DB");
        LOGGER.debug("DB config: url="+DB_URL+", username="+DB_USERNAME);
        dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
    }


    public List<Issue> getIssueBriefly(int from, int len, Connection conn) throws SQLException {
        String sql = "SELECT issue_code, issue_name, status, for_problem, issue_type FROM prob_issue WHERE issue_code>=? ORDER BY issue_code LIMIT ?;";
        PreparedStatement psmt = conn.prepareStatement(sql);
        psmt.setInt(1, from);
        psmt.setInt(2, len);
        ResultSet rs = psmt.executeQuery();

        List<Issue> pList = new Vector<>();
        while (rs.next()) {
            Issue issue = new Issue();
            issue.setIssueCode(rs.getInt(ISSUE_CODE));
            issue.setIssueName(rs.getString(ISSUE_NANE));
            issue.setStatus(rs.getInt(ISSUE_STATUS));
            issue.setForProblem(rs.getInt(FOR_PROBLEM));
            issue.setType(rs.getInt(ISSUE_TYPE));
            pList.add(issue);
        }
        return pList;
    }

    public void addNewIssue(Issue issue, String authorId, Connection conn) throws SQLException {
        String sql = "INSERT INTO prob_issue (issue_name, issue_content, vote, status, for_problem, issue_type, author, written_at) VALUES (?, ?, 0, ?, ?, ?, ?, ?);";
        PreparedStatement psmt = conn.prepareStatement(sql);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        psmt.setString(1, issue.getIssueName());
        psmt.setString(2, issue.getContent());
        psmt.setInt(3, issue.getStatusInt());
        psmt.setInt(4, issue.getForProblem());
        psmt.setInt(5, issue.getTypeInt());
        psmt.setString(6, authorId);
        psmt.setTimestamp(7, timestamp);

        psmt.execute();
    }
}
