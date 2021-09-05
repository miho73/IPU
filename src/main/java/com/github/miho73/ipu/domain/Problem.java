package com.github.miho73.ipu.domain;

public class Problem {
    private long code;
    private String name, category, difficulty, content, solution, answer, hint, extrTabs, tags;
    private boolean hasHint;

    public Problem(long code, String name, String category, String difficulty, String tags) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.difficulty = difficulty;
        this.tags = tags;
    }

    public Problem(long code, String name, String content, String solution, String answer, String hint, String extrTabs, boolean hasHint) {
        this.code = code;
        this.name = name;
        this.content = content;
        this.solution = solution;
        this.answer = answer;
        this.hint = hint;
        this.extrTabs = extrTabs;
        this.hasHint = hasHint;
    }

    public long getCode() {
        return code;
    }
    public void setCode(long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getSolution() {
        return solution;
    }
    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getHint() {
        return hint;
    }
    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getExtrTabs() {
        return extrTabs;
    }
    public void setExtrTabs(String extrTabs) {
        this.extrTabs = extrTabs;
    }

    public boolean isHasHint() {
        return hasHint;
    }
    public void setHasHint(boolean hasHint) {
        this.hasHint = hasHint;
    }

    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
}
