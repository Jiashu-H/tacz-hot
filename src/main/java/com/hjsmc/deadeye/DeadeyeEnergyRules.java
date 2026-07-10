package com.hjsmc.deadeye;

final class DeadeyeEnergyRules {
    static final float MAX_ENERGY = 100.0F;

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
}
