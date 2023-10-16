package com.mycompany.app;

import java.util.HashMap;
import java.util.List;


public class GameResult {
    public
    String[][] matrix;
    public double reward;
    public
    HashMap<String, List<String>> applied_winning_combinations;
    public String applied_bonus_symbol;

    public GameResult(
            String[][] matrix, double reward, HashMap<String, List<String>> applied_winning_combinations, String applied_bonus_symbol) {
        this.matrix = matrix;
        this.reward = reward;
        this.applied_winning_combinations = applied_winning_combinations;
        this.applied_bonus_symbol = applied_bonus_symbol;
    }
}

