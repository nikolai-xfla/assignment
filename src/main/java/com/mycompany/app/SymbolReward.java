package com.mycompany.app;

public class SymbolReward {

    public Double reward_multiplier;
    public String when;
    public Double count;
    public String group;
    public Integer repeatingTimes;

    public SymbolReward(Double reward_multiplier, Integer repeat) {
        this.reward_multiplier = reward_multiplier;
        this.repeatingTimes = repeat;
    }
}
