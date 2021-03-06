package com.github.miho73.ipu.domain;

import org.json.JSONArray;

import java.sql.Timestamp;

public class Problem {
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public enum PROBLEM_DIFFICULTY {
        UNSET,
        UNRATED,
        BRONZE,
        SILVER,
        GOLD,
        SAPPHIRE,
        RUBY,
        DIAMOND
    }
    public enum PROBLEM_CATEGORY {
        ETCETERA,
        ALGEBRA,
        NUMBER_THEORY,
        COMBINATORICS,
        GEOMETRY,
        PHYSICS,
        CHEMISTRY,
        BIOLOGY,
        EARTH_SCIENCE;
    }
    public enum JUDGEMENT_TYPE {
        SELF_JUDGE,
        TEXT_JUDGE,
        FRACTION_JUDGE
    }
    private int code;
    private String name;
    private PROBLEM_DIFFICULTY difficulty;
    private PROBLEM_CATEGORY category;

    private String content, solution;
    private boolean active;
    private String tags, author_name;
    private Timestamp added_at, last_modified;
    private JSONArray answer;

    public String getAuthor_name() {
        return author_name;
    }
    public void setAuthor_name(String author_name) {
        this.author_name = author_name;
    }

    public JSONArray getAnswer() {
        return answer;
    }
    public void setAnswer(String answer) {
        this.answer = new JSONArray(answer);
    }
    public void setAnswer(JSONArray answer) {
        this.answer = answer;
    }

    public Timestamp getAdded_at() {
        return added_at;
    }
    public void setAdded_at(Timestamp added_at) {
        this.added_at = added_at;
    }

    public Timestamp getLast_modified() {
        return last_modified;
    }
    public void setLast_modified(Timestamp last_modified) {
        this.last_modified = last_modified;
    }

    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public PROBLEM_DIFFICULTY getDifficulty() {
        return difficulty;
    }
    public String getDifficultyCode() {
        return switch (difficulty) {
            case UNSET    -> "unse";
            case UNRATED  -> "unra";
            case BRONZE   -> "broz";
            case SILVER   -> "silv";
            case GOLD     -> "gold";
            case SAPPHIRE -> "sapp";
            case RUBY     -> "ruby";
            case DIAMOND  -> "diam";
        };
    }
    public void setDifficulty(PROBLEM_DIFFICULTY difficulty) {
        this.difficulty = difficulty;
    }
    public void setDifficulty(String difficulty) {
        switch (difficulty) {
            case "unse" -> setDifficulty(PROBLEM_DIFFICULTY.UNSET);
            case "unra" -> setDifficulty(PROBLEM_DIFFICULTY.UNRATED);
            case "broz" -> setDifficulty(PROBLEM_DIFFICULTY.BRONZE);
            case "silv" -> setDifficulty(PROBLEM_DIFFICULTY.SILVER);
            case "gold" -> setDifficulty(PROBLEM_DIFFICULTY.GOLD);
            case "sapp" -> setDifficulty(PROBLEM_DIFFICULTY.SAPPHIRE);
            case "ruby" -> setDifficulty(PROBLEM_DIFFICULTY.RUBY);
            case "diam" -> setDifficulty(PROBLEM_DIFFICULTY.DIAMOND);
        }
    }

    public PROBLEM_CATEGORY getCategory() {
        return category;
    }
    public String getCategoryCode() {
        return switch (category) {
            case ETCETERA       -> "etce";
            case ALGEBRA        -> "alge";
            case NUMBER_THEORY  -> "numb";
            case COMBINATORICS  -> "comb";
            case GEOMETRY       -> "geom";
            case PHYSICS        -> "phys";
            case CHEMISTRY      -> "chem";
            case BIOLOGY        -> "biol";
            case EARTH_SCIENCE  -> "eart";
        };
    }
    public void setCategory(PROBLEM_CATEGORY category) {
        this.category = category;
    }
    public void setCategory(String category) {
        switch (category) {
            case "etce"  -> setCategory(PROBLEM_CATEGORY.ETCETERA);
            case "alge" -> setCategory(PROBLEM_CATEGORY.ALGEBRA);
            case "numb" -> setCategory(PROBLEM_CATEGORY.NUMBER_THEORY);
            case "comb" -> setCategory(PROBLEM_CATEGORY.COMBINATORICS);
            case "geom" -> setCategory(PROBLEM_CATEGORY.GEOMETRY);
            case "phys" -> setCategory(PROBLEM_CATEGORY.PHYSICS);
            case "chem" -> setCategory(PROBLEM_CATEGORY.CHEMISTRY);
            case "biol" -> setCategory(PROBLEM_CATEGORY.BIOLOGY);
            case "eart" -> setCategory(PROBLEM_CATEGORY.EARTH_SCIENCE);
        }
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

    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
}