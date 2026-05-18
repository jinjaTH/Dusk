package com.dusk.tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DreadTracker {

    public static final int NYCTO = 0;

    private static final Map<UUID, Double>  dreadScores = new HashMap<>();
    private static final Map<UUID, Integer> stages      = new HashMap<>();

    public static void init(UUID player) {
        dreadScores.put(player, 0.0);
        stages     .put(player, 0);
    }

    public static void remove(UUID player) {
        dreadScores.remove(player);
        stages     .remove(player);
    }

    public static double getDread(UUID player, int phobiaId) {
        return dreadScores.getOrDefault(player, 0.0);
    }

    public static void setDread(UUID player, int phobiaId, double value) {
        dreadScores.put(player, value);
    }

    public static int getStage(UUID player, int phobiaId) {
        return stages.getOrDefault(player, 0);
    }

    public static int setStageAndGetOld(UUID player, int phobiaId, int newStage) {
        int old = stages.getOrDefault(player, 0);
        stages.put(player, newStage);
        return old;
    }

    public static boolean anyInNyctophobia() {
        return stages.values().stream().anyMatch(s -> s > 0);
    }
}
