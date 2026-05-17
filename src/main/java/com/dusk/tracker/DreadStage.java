package com.dusk.tracker;

public enum DreadStage {
    CALM(0),
    S1(1),
    S2(2),
    S3(3),
    S4(4),
    S5(5),
    S6(6);

    public final int level;

    DreadStage(int level) {
        this.level = level;
    }

    public static DreadStage fromLevel(int level) {
        for (DreadStage s : values()) {
            if (s.level == level) return s;
        }
        return CALM;
    }
}
