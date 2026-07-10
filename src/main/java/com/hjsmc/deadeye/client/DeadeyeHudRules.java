package com.hjsmc.deadeye.client;

final class DeadeyeHudRules {
    private DeadeyeHudRules() {
    }

    static boolean shouldShowEnergyText(boolean enabled, float energy) {
        return enabled && Math.round(energy) < 100;
    }
}
