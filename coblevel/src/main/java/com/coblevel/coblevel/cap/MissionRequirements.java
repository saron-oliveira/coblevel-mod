package com.coblevel.coblevel.cap;

/**
 * Defines the mission requirements for each phase.
 * Requirements scale with phase index to become progressively harder.
 */
public record MissionRequirements(int captureCount, int trainerCount, long xpAmount) {

    /**
     * Generates scaled requirements for a given phase index (0-based).
     *
     * Phase 0 (cap 15):  capture 5,  trainers 3,  xp 500
     * Each phase:        +3 capture, +2 trainers, +500 xp
     *
     * Gym is always required (1 per phase).
     */
    public static MissionRequirements forPhase(int phaseIndex) {
        int phase = Math.max(0, phaseIndex);
        int captureCount = 5 + (phase * 3);
        int trainerCount = 3 + (phase * 2);
        long xpAmount    = 500L + (phase * 500L);
        return new MissionRequirements(captureCount, trainerCount, xpAmount);
    }

    /**
     * Returns a human-readable summary of the requirements.
     */
    public String describe() {
        return String.format(
            "Capturar %d Pokémons | Vencer %d Treinadores | Limpar o Ginásio | Entregar %d XP",
            captureCount, trainerCount, xpAmount
        );
    }
}
