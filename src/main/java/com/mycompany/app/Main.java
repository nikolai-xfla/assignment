package com.mycompany.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class Main {
    public static int columns = 3;
    public static int rows = 3;

    public int row;
    public int col;

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Provide required arguments: (amount, config_path)");
            return;
        }

        double amount = Double.parseDouble(args[0]);
        String configPath = args[1];

        BufferedReader reader = new BufferedReader(new FileReader(configPath));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        String ls = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        objectMapper = new ObjectMapper();
        Codebeautify config = objectMapper.readValue(stringBuilder.toString(), Codebeautify.class);
        Main rand = new Main();
        rows = config.rows;
        columns = config.columns;
        var resultMatrix = rand.generateResultMatrix(config);
        var str = rand.calcGameResult(resultMatrix, config, amount);
        System.out.println(str);
    }

    public String calcGameResult(String[][] matrix, Codebeautify config, double betAmount) throws JsonProcessingException {

        Symbol bonusSymbol = null;

        var repeatedSymbols = new HashMap<String, Integer>();
        for (String[] strings : matrix) {
            for (int col = 0; col < matrix[0].length; col++) {
                String cellSymbol = strings[col];
                if (repeatedSymbols.containsKey(cellSymbol)) {
                    var currentValue = repeatedSymbols.get(cellSymbol);
                    repeatedSymbols.put(cellSymbol, ++currentValue);
                } else {
                    repeatedSymbols.put(cellSymbol, 1);
                }
            }
        }

        var winCombinations = new HashMap<String, SymbolReward>();

        for (String key : repeatedSymbols.keySet()) {
            switch (repeatedSymbols.get(key)) {
                case 3 -> winCombinations.put(key, new SymbolReward(config.symbols.get(key).reward_multiplier, 3));
                case 4 -> winCombinations.put(key, new SymbolReward(config.symbols.get(key).reward_multiplier, 4));
                case 5 -> winCombinations.put(key, new SymbolReward(config.symbols.get(key).reward_multiplier, 5));
                case 6 -> winCombinations.put(key, new SymbolReward(config.symbols.get(key).reward_multiplier, 6));
                case 7 -> winCombinations.put(key, new SymbolReward(config.symbols.get(key).reward_multiplier, 7));
                case 8 -> winCombinations.put(key, new SymbolReward(config.symbols.get(key).reward_multiplier, 8));
                case 9 -> winCombinations.put(key, new SymbolReward(config.symbols.get(key).reward_multiplier, 9));
            }

            var symbol = config.symbols.get(key);
            if (symbol != null && symbol.type.equals("bonus")) {
                bonusSymbol = config.symbols.get(key);
                bonusSymbol.bonusName = key;
            }
        }

        double winAmount = 0;
        HashMap<String, List<String>> winSymbols = new HashMap<>();
        for (var key : winCombinations.keySet()) {
            winSymbols.put(key, List.of("same_symbol_" + winCombinations.get(key).repeatingTimes + "_times"));
            var symbol = config.symbols.get(key);
            //           bet_amount  reward(symbol_A)           reward(same_symbol_5_times)
            winAmount += betAmount * symbol.reward_multiplier * winCombinations.get(key).reward_multiplier;

        }

        if (bonusSymbol != null) {
            if (bonusSymbol.impact.equals("multiply_reward")) {
                winAmount *= bonusSymbol.reward_multiplier;
            } else if (bonusSymbol.impact.equals("extra_bonus")) {
                winAmount += bonusSymbol.extra;
            }
        }

        var gameResult = new GameResult(matrix, winAmount, winSymbols, bonusSymbol != null ? bonusSymbol.bonusName : null);

        return objectMapper.writeValueAsString(gameResult);
    }


    public String[][] generateResultMatrix(Codebeautify config) {
        String[][] matrixResult = new String[rows][columns];
        var bonusApplied = false;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                this.row = row;
                this.col = col;

                Probabilities.Cell cell = config.probabilities.standard_symbols.stream()
                        .filter(symbol -> ((symbol.column == this.col) && (symbol.row == this.row)))
                        .findAny().get();

                int commonProbabilityForCell = 0;
                for (String key : cell.symbols.keySet()) {
                    commonProbabilityForCell += cell.symbols.get(key);
                }

                var bonusSymbolsProbabilities = new HashMap<String, Double>();
                for (String key : config.probabilities.bonus_symbols.symbols.keySet()) {
                    bonusSymbolsProbabilities.put(key, ((config.probabilities.bonus_symbols.symbols.get(key) / commonProbabilityForCell) * 100));
                }

                var maxForBonusSymbols = Collections.max(bonusSymbolsProbabilities.entrySet(), Map.Entry.comparingByValue()).getValue();
                var randomProbabilityBonusSymbols = ThreadLocalRandom.current().nextInt(0, maxForBonusSymbols.intValue());

                var symbolsProbabilities = new HashMap<String, Double>();
                for (String key : cell.symbols.keySet()) {
                    symbolsProbabilities.put(key, (cell.symbols.get(key) / commonProbabilityForCell) * 100);
                }

                var max = Collections.max(symbolsProbabilities.entrySet(), Map.Entry.comparingByValue()).getValue();
                var randomProbability = ThreadLocalRandom.current().nextInt(0, max.intValue());

                var chosenSymbol = getSymbolWin(symbolsProbabilities, randomProbability);
                if (ThreadLocalRandom.current().nextBoolean() && !bonusApplied) {
                    chosenSymbol = getSymbolWin(bonusSymbolsProbabilities, randomProbabilityBonusSymbols);
                    bonusApplied = true;
                }

                matrixResult[row][col] = chosenSymbol;
            }
        }

        return matrixResult;
    }

    private static String getSymbolWin(HashMap<String, Double> symbolsProbabilities, int randomProbability) {
        var previousProbability = 0.0;
        var chosenSymbol = "";
        for (String key : symbolsProbabilities.keySet()) {
            var currentProbability = symbolsProbabilities.get(key);
            if (randomProbability < currentProbability && randomProbability > previousProbability) {
                chosenSymbol = key;
            }
            previousProbability = currentProbability;
        }
        return chosenSymbol;
    }
}
