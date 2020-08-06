package com.janus.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public enum Difficulty {

    Default(0, 0),
    Easy(3, 0),
    Medium(0, 2),
    Hard(0.5, 5),
    Insane(0.25, 10),
    Zezima(0.1, 20);

    private double xpRate;
    private int drBoost;

    Difficulty(double xpBoost, int drBoost) {
        this.xpRate = xpBoost;
        this.drBoost = drBoost;
    }

    private static final ImmutableSet<Difficulty> LOW = Sets.immutableEnumSet(Default, Easy, Medium);
    private static final ImmutableSet<Difficulty> HIGH = Sets.immutableEnumSet(Hard, Insane, Zezima);

    public boolean lowDifficulty() {
        return LOW.contains(this);
    }

    public boolean highDifficulty() {
        return HIGH.contains(this);
    }

    public double getDropRateModifier() {
        return drBoost;
    }

    public double getXpRate() {
        return xpRate; }

}
