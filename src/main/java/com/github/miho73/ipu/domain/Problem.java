package com.github.miho73.ipu.domain;

public class Problem {
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
        EARTH_SCIENCE
    }
    private int code;
    private String name;
    private PROBLEM_DIFFICULTY difficulty;
    private PROBLEM_CATEGORY category;
    private String content, solution, answer, hint;
    private boolean hasHint;
    private String externalTabs, tags;

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

    public boolean isHasHint() {
        return hasHint;
    }
    public void setHasHint(boolean hasHint) {
        this.hasHint = hasHint;
    }

    public String getExternalTabs() {
        return externalTabs;
    }
    public void setExternalTabs(String externalTabs) {
        this.externalTabs = externalTabs;
    }

    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }
}