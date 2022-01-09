package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.Issue;
import com.github.miho73.ipu.repositories.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service("IssueService")
public class IssueService {
    @Autowired IssueRepository issueRepository;

    public List<Issue> getIssueList(int from, int length) throws SQLException {
        Connection connection = issueRepository.openConnection();
        List<Issue> ret = issueRepository.getIssueBriefly(from, length, connection);
        issueRepository.close(connection);
        return ret;
    }

    public void addNewIssue(Issue issue, String authorId) throws SQLException {
        Connection connection = issueRepository.openConnectionForEdit();
        issueRepository.addNewIssue(issue, authorId, connection);
        issueRepository.commitAndClose(connection);
    }
}
