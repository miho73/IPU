package com.github.miho73.ipu.library;

import com.github.miho73.ipu.domain.Problem;
import org.springframework.stereotype.Component;

import java.util.Hashtable;

@Component
public class ExperienceSystem {
    private final Hashtable<Problem.PROBLEM_DIFFICULTY, Long> ExpTable = new Hashtable<>() {
        {
            put(Problem.PROBLEM_DIFFICULTY.UNSET, 0L);
            put(Problem.PROBLEM_DIFFICULTY.UNRATED, 1000L);
            put(Problem.PROBLEM_DIFFICULTY.BRONZE, 2000L);
            put(Problem.PROBLEM_DIFFICULTY.SILVER, 4000L);
            put(Problem.PROBLEM_DIFFICULTY.GOLD, 8000L);
            put(Problem.PROBLEM_DIFFICULTY.SAPPHIRE, 16000L);
            put(Problem.PROBLEM_DIFFICULTY.RUBY, 32000L);
            put(Problem.PROBLEM_DIFFICULTY.DIAMOND, 64000L);
        }
    };

    private final Hashtable<Problem.PROBLEM_DIFFICULTY, Long> waDiv = new Hashtable<>() {
        {
            put(Problem.PROBLEM_DIFFICULTY.UNSET, 0L);
            put(Problem.PROBLEM_DIFFICULTY.UNRATED, 2L);
            put(Problem.PROBLEM_DIFFICULTY.BRONZE, 4L);
            put(Problem.PROBLEM_DIFFICULTY.SILVER, 8L);
            put(Problem.PROBLEM_DIFFICULTY.GOLD, 16L);
            put(Problem.PROBLEM_DIFFICULTY.SAPPHIRE, 32L);
            put(Problem.PROBLEM_DIFFICULTY.RUBY, 64L);
            put(Problem.PROBLEM_DIFFICULTY.DIAMOND, 128L);
        }
    };

    private final Hashtable<Long, Double> powTable = new Hashtable<>() {
        {
            put( 0L, 1.0);
            put( 1L, 0.5);
            put( 2L, 0.25);
            put( 3L, 0.125);
            put( 4L, 0.0625);
            put( 5L, 0.03125);
            put( 6L, 0.015625);
            put( 7L, 0.0078125);
            put( 8L, 0.0039063);
            put( 9L, 0.0019531);
            put(10L, 0.0009766);
        }
    };

    private double getDC(long solves) {
        if(solves >= 0 && solves <= 10) {
            return powTable.get(solves);
        }
        return 0.01;
    }

    public long getExp(Problem.PROBLEM_DIFFICULTY difficulty, long solves) {
        return (long) Math.ceil(ExpTable.get(difficulty) * getDC(solves));
    }

    public long toWa(long exp, Problem.PROBLEM_DIFFICULTY difficulty) {
        return exp*waDiv.get(difficulty);
    }
}
