package com.coblevel.coblevel.cap;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the progression of level caps.
 * 15 -> 20 -> 25 -> ... -> 50 -> 60 -> 70 -> ... -> 120
 */
public class CapProgression {

    public static final int INITIAL_CAP = 15;
    public static final int MAX_CAP = 120;

    private static final List<Integer> CAP_LEVELS = new ArrayList<>();

    static {
        // 15 to 50, step 5
        for (int i = 15; i <= 50; i += 5) {
            CAP_LEVELS.add(i);
        }
        // 60 to 120, step 10
        for (int i = 60; i <= 120; i += 10) {
            CAP_LEVELS.add(i);
        }
    }

    /**
     * Returns the cap level at a given phase index (0-based).
     */
    public static int getCapAtPhase(int phaseIndex) {
        if (phaseIndex < 0) return INITIAL_CAP;
        if (phaseIndex >= CAP_LEVELS.size()) return MAX_CAP;
        return CAP_LEVELS.get(phaseIndex);
    }

    /**
     * Returns the total number of phases.
     */
    public static int getTotalPhases() {
        return CAP_LEVELS.size();
    }

    /**
     * Returns the next cap after the current one, or MAX_CAP if already at max.
     */
    public static int getNextCap(int currentCap) {
        for (int i = 0; i < CAP_LEVELS.size() - 1; i++) {
            if (CAP_LEVELS.get(i) == currentCap) {
                return CAP_LEVELS.get(i + 1);
            }
        }
        return MAX_CAP;
    }

    /**
     * Returns the phase index for a given cap value, or -1 if not found.
     */
    public static int getPhaseForCap(int cap) {
        return CAP_LEVELS.indexOf(cap);
    }

    public static List<Integer> getAllCaps() {
        return List.copyOf(CAP_LEVELS);
    }

    public static boolean isMaxCap(int cap) {
        return cap >= MAX_CAP;
    }
}
