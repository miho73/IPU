package com.github.miho73.ipu.library;

import com.github.miho73.ipu.domain.Problem;
import org.springframework.stereotype.Component;

import java.util.Hashtable;

@Component
public class ExperienceSystem {
    private final Hashtable<Problem.PROBLEM_DIFFICULTY, Integer> ExpTable = new Hashtable<>() {
        {
            put(Problem.PROBLEM_DIFFICULTY.UNSET, 0);
            put(Problem.PROBLEM_DIFFICULTY.UNRATED, 500);
            put(Problem.PROBLEM_DIFFICULTY.BRONZE, 1000);
            put(Problem.PROBLEM_DIFFICULTY.SILVER, 2000);
            put(Problem.PROBLEM_DIFFICULTY.GOLD, 4000);
            put(Problem.PROBLEM_DIFFICULTY.SAPPHIRE, 8000);
            put(Problem.PROBLEM_DIFFICULTY.RUBY, 16000);
            put(Problem.PROBLEM_DIFFICULTY.DIAMOND, 32000);
        }
    };

    private final Hashtable<Integer, Double> powTable = new Hashtable<>() {
        {
            put( 0, 1.0);
            put( 1, 0.5);
            put( 2, 0.25);
            put( 3, 0.125);
            put( 4, 0.0625);
            put( 5, 0.03125);
            put( 6, 0.015625);
            put( 7, 0.0078125);
            put( 8, 0.0039063);
            put( 9, 0.0019531);
            put(10, 0.0009766);
        }
    };

    private double getDC(int solves) {
        if(solves >= 0 && solves <= 10) {
            return powTable.get(solves);
        }
        return 0.01;
    }

    public int getExp(Problem.PROBLEM_DIFFICULTY difficulty, int solves) {
        return (int) Math.ceil(ExpTable.get(difficulty) * getDC(solves));
    }
}
