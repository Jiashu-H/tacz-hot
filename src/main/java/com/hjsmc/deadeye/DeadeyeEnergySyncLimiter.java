package com.hjsmc.deadeye;

/**
 * Wall-clock rate limiter for intermediate energy display updates.
 * Boundary values bypass throttling; accumulated stalls never create bursts.
 */
final class DeadeyeEnergySyncLimiter {
    private static final double NANOS_PER_SECOND = 1_000_000_000.0D;
    private long lastUpdateNanos;
    private double credit;
    private boolean initialized;

    void reset(long nowNanos) {
        lastUpdateNanos = nowNanos;
        credit = 0.0D;
        initialized = true;
    }

    boolean shouldSync(float current, float lastSynced, long nowNanos, int packetsPerSecond) {
        if (!initialized) {
            reset(nowNanos);
        }

        if (nowNanos > lastUpdateNanos) {
            long elapsedNanos = nowNanos - lastUpdateNanos;
            lastUpdateNanos = nowNanos;
            int rate = Math.max(DeadeyeEnergyRules.MIN_ENERGY_SYNC_RATE,
                    Math.min(DeadeyeEnergyRules.MAX_ENERGY_SYNC_RATE, packetsPerSecond));
            credit += elapsedNanos / NANOS_PER_SECOND * rate;
        }

        if (Math.abs(current - lastSynced) <= DeadeyeEnergyRules.ENERGY_SYNC_EPSILON) {
            credit = Math.min(credit, 1.0D);
            return false;
        }
        if (DeadeyeEnergyRules.isBoundaryEnergy(current)
                || DeadeyeEnergyRules.isBoundaryEnergy(lastSynced)) {
            reset(nowNanos);
            return true;
        }
        if (credit < 1.0D) {
            return false;
        }

        double remainingCredit = credit - 1.0D;
        credit = remainingCredit < 1.0D ? remainingCredit : 0.0D;
        return true;
    }
}
