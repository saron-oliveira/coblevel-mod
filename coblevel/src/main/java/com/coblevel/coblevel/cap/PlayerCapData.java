package com.coblevel.coblevel.cap;

import net.minecraft.nbt.CompoundTag;

/**
 * Stores and manages a single player's level cap data.
 * Saved to player NBT via capability.
 */
public class PlayerCapData {

    private int currentCap;
    private int capturedPokemons;
    private int defeatedTrainers;
    private boolean gymCleared;       // cleared gym at current cap
    private long deliveredXp;

    public PlayerCapData() {
        this.currentCap = CapProgression.INITIAL_CAP;
        this.capturedPokemons = 0;
        this.defeatedTrainers = 0;
        this.gymCleared = false;
        this.deliveredXp = 0;
    }

    // ---- Progress checks ----

    public boolean hasCompletedAllMissions(int phaseIndex) {
        MissionRequirements req = MissionRequirements.forPhase(phaseIndex);
        return capturedPokemons >= req.captureCount()
                && defeatedTrainers >= req.trainerCount()
                && gymCleared
                && deliveredXp >= req.xpAmount();
    }

    public void resetMissionProgress() {
        this.capturedPokemons = 0;
        this.defeatedTrainers = 0;
        this.gymCleared = false;
        this.deliveredXp = 0;
    }

    // ---- Getters & Setters ----

    public int getCurrentCap() { return currentCap; }
    public void setCurrentCap(int cap) { this.currentCap = cap; }

    public int getCapturedPokemons() { return capturedPokemons; }
    public void addCapturedPokemon() { this.capturedPokemons++; }

    public int getDefeatedTrainers() { return defeatedTrainers; }
    public void addDefeatedTrainer() { this.defeatedTrainers++; }

    public boolean isGymCleared() { return gymCleared; }
    public void setGymCleared(boolean cleared) { this.gymCleared = cleared; }

    public long getDeliveredXp() { return deliveredXp; }
    public void addDeliveredXp(long amount) { this.deliveredXp += amount; }

    // ---- NBT ----

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("currentCap", currentCap);
        tag.putInt("capturedPokemons", capturedPokemons);
        tag.putInt("defeatedTrainers", defeatedTrainers);
        tag.putBoolean("gymCleared", gymCleared);
        tag.putLong("deliveredXp", deliveredXp);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.currentCap = tag.contains("currentCap") ? tag.getInt("currentCap") : CapProgression.INITIAL_CAP;
        this.capturedPokemons = tag.getInt("capturedPokemons");
        this.defeatedTrainers = tag.getInt("defeatedTrainers");
        this.gymCleared = tag.getBoolean("gymCleared");
        this.deliveredXp = tag.getLong("deliveredXp");
    }
}
