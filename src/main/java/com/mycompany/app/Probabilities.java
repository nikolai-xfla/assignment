package com.mycompany.app;

import java.util.ArrayList;
import java.util.Map;

public class Probabilities {
    public ArrayList<Cell> standard_symbols = new ArrayList<Cell>();
    public Bonus_symbols bonus_symbols;

    public static class Cell {
        public int column;
        public int row;
        public Map<String, Double> symbols;
    }
}