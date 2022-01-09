package com.github.miho73.ipu.domain;

import java.sql.Timestamp;

public class Issue {
    private int issueCode, vote, forProblem;

    public int getForProblem() {
        return forProblem;
    }

    public void setForProblem(int forProblem) {
        this.forProblem = forProblem;
    }

    public String getIssueName() {
        return issueName;
    }

    public void setIssueName(String issueName) {
        this.issueName = issueName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ISSUE_TYPE getType() {
        return type;
    }
    public int getTypeInt() {
        return switch (type) {
            case MISTYPE -> 0;
            case AMBIGUOUS_MEANING -> 1;
            case INSUFFICIENT_PROBLEM -> 2;
            case SOLUTION_NOT_CORRECT -> 3;
            case ETC -> 4;
            case COPYRIGHT -> 5;
        };
    }

    public void setType(ISSUE_TYPE type) {
        this.type = type;
    }
    public void setType(int type) {
        switch (type) {
            case 0 -> this.type = ISSUE_TYPE.MISTYPE;
            case 1 -> this.type = ISSUE_TYPE.AMBIGUOUS_MEANING;
            case 2 -> this.type = ISSUE_TYPE.INSUFFICIENT_PROBLEM;
            case 3 -> this.type = ISSUE_TYPE.SOLUTION_NOT_CORRECT;
            case 4 -> this.type = ISSUE_TYPE.ETC;
            case 5 -> this.type = ISSUE_TYPE.COPYRIGHT;
        }
    }

    private String openBy, issueName, content;
    private Timestamp openAt;
    private ISSUE_STATUS status;
    private ISSUE_TYPE type;

    public enum ISSUE_STATUS {
        OPEN,
        PENDING_APPLY,
        CLOSED,
        NEED_DISCUSSION
    }
    enum ISSUE_TYPE {
        MISTYPE,
        AMBIGUOUS_MEANING,
        INSUFFICIENT_PROBLEM,
        SOLUTION_NOT_CORRECT,
        ETC,
        COPYRIGHT
    }

    public int getIssueCode() {
        return issueCode;
    }

    public void setIssueCode(int issueCode) {
        this.issueCode = issueCode;
    }

    public String getOpenBy() {
        return openBy;
    }

    public void setOpenBy(String openBy) {
        this.openBy = openBy;
    }

    public Timestamp getOpenAt() {
        return openAt;
    }

    public void setOpenAt(Timestamp openAt) {
        this.openAt = openAt;
    }

    public ISSUE_STATUS getStatus() {
        return status;
    }
    public int getStatusInt() {
        return switch (status) {
            case OPEN -> 0;
            case PENDING_APPLY -> 1;
            case CLOSED -> 2;
            case NEED_DISCUSSION -> 3;
        };
    }

    public void setStatus(ISSUE_STATUS status) {
        this.status = status;
    }
    public void setStatus(int status) {
        switch (status) {
            case 0 -> this.status = ISSUE_STATUS.OPEN;
            case 1 -> this.status = ISSUE_STATUS.PENDING_APPLY;
            case 2 -> this.status = ISSUE_STATUS.CLOSED;
            case 3 -> this.status = ISSUE_STATUS.NEED_DISCUSSION;
        }
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }
}
