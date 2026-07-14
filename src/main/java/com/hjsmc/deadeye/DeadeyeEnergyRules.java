package com.hjsmc.deadeye;

final class DeadeyeEnergyRules {
    static final float MAX_ENERGY = 100.0F;
    static final int DEFAULT_ENERGY_SYNC_RATE = 5;
    static final float ENERGY_SYNC_EPSILON = 0.001F;
    private static final int NORMAL_TICKS_PER_SECOND = 20;

    private DeadeyeEnergyRules() {
    }

    static boolean canActivate(float energy, boolean infiniteEnergy) {
        return infiniteEnergy || energy > 0.0F;
    }

    static float normalizedEnergy(float energy, boolean infiniteEnergy) {
        return infiniteEnergy ? MAX_ENERGY : energy;
    }

    static float energyAfterDrain(float energy, float drain, boolean infiniteEnergy) {
        return infiniteEnergy ? MAX_ENERGY : Math.max(0.0F, energy - drain);
    }

    /**
     * Converts a configured packets-per-second rate (1-5) into the minimum
     * server-tick gap between mid-range energy display syncs.
     */
    static int syncIntervalTicks(int packetsPerSecond) {
        int rate = Math.max(1, Math.min(5, packetsPerSecond));
        return Math.max(1, Math.round(NORMAL_TICKS_PER_SECOND / (float) rate));
    }

    /**
     * Whether the client display needs a fresh energy value.
     * Boundaries (0 / 100) sync immediately; mid-range values are throttled.
     */
    static boolean shouldSyncEnergy(float current, float lastSynced, int ticksSinceLastSync,
                                    int minIntervalTicks) {
        if (Math.abs(current - lastSynced) <= ENERGY_SYNC_EPSILON) {
            return false;
        }
        if (isBoundaryEnergy(current) || isBoundaryEnergy(lastSynced)) {
            return true;
        }
        return ticksSinceLastSync >= minIntervalTicks;
    }

    static boolean isBoundaryEnergy(float energy) {
        return energy <= 0.0F || energy >= MAX_ENERGY;
    }
}
