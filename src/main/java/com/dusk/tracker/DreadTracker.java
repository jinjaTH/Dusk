package com.dusk.tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DreadTracker {

    public static final int NYCTO = 0;
    public static final int ACRO  = 1;
    public static final int THALA = 2;

    private static final Map<UUID, double[]> dreadScores  = new HashMap<>();
    private static final Map<UUID, long[]>   exposureTicks = new HashMap<>();
    private static final Map<UUID, int[]>    stages        = new HashMap<>();

    public static void init(UUID player) {
        dreadScores .put(player, new double[3]);
        exposureTicks.put(player, new long[3]);
        stages       .put(player, new int[3]);
    }

    public static void remove(UUID player) {
        dreadScores .remove(player);
        exposureTicks.remove(player);
        stages       .remove(player);
    }

    public static double getDread(UUID player, int phobiaId) {
        double[] d = dreadScores.get(player);
        return d == null ? 0 : d[phobiaId];
    }

    public static void setDread(UUID player, int phobiaId, double value) {
        dreadScores.computeIfAbsent(player, k -> new double[3])[phobiaId] = value;
    }

    public static long getExposure(UUID player, int phobiaId) {
        long[] t = exposureTicks.get(player);
        return t == null ? 0 : t[phobiaId];
    }

    public static void setExposure(UUID player, int phobiaId, long value) {
        exposureTicks.computeIfAbsent(player, k -> new long[3])[phobiaId] = value;
    }

    public static int getStage(UUID player, int phobiaId) {
        int[] s = stages.get(player);
        return s == null ? 0 : s[phobiaId];
    }

    public static int setStageAndGetOld(UUID player, int phobiaId, int newStage) {
        int[] s = stages.computeIfAbsent(player, k -> new int[3]);
        int old = s[phobiaId];
        s[phobiaId] = newStage;
        return old;
    }

    public static boolean anyInNyctophobia() {
        for (int[] s : stages.values()) {
            if (s[NYCTO] > 0) return true;
        }
        return false;
    }
}
