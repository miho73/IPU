package com.github.miho73.ipu.services;

import com.github.miho73.ipu.domain.Issue;
import com.github.miho73.ipu.library.exceptions.ForbiddenException;
import com.github.miho73.ipu.library.exceptions.ResourceNotFoundException;
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

    public Issue getIssue(int issueCode) throws SQLException {
        Connection connection = issueRepository.openConnection();
        Issue issue = issueRepository.getIssue(issueCode, connection);
        issueRepository.close(connection);
        return issue;
    }

    public void updateIssueTitle(int issueCode, String newName, String id) throws ResourceNotFoundException, SQLException, ForbiddenException {
        Issue issue = getIssue(issueCode);
        if(issue == null) throw new ResourceNotFoundException("issue not found");
        if(!issue.getAuthor().equals(id)) throw new ForbiddenException("only author can change issue name");
        if(newName.equals("")) throw new IllegalArgumentException("name cannot be empty");

        Connection connection = issueRepository.openConnectionForEdit();
        issueRepository.updateValue(issueRepository.ISSUE_NANE, newName, issueCode, connection);
        issueRepository.commitAndClose(connection);
    }
}
