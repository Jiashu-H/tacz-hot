package com.hjsmc.deadeye;

public final class DeadeyeEnergyRulesTest {
    private DeadeyeEnergyRulesTest() {
    }

    public static void main(String[] args) {
        infiniteEnergyAllowsActivationAtZero();
        infiniteEnergyNormalizesEnergyToFull();
        infiniteEnergyPreventsDrain();
        ordinaryEnergyStillDrainsAndStopsAtZero();
        syncIntervalMatchesPacketsPerSecond();
        midRangeEnergyIsThrottledUntilInterval();
        boundaryEnergySyncsImmediately();
        unchangedEnergyNeverSyncs();
        leavingFullEnergySyncsImmediately();
    }

    private static void infiniteEnergyAllowsActivationAtZero() {
        assertTrue(DeadeyeEnergyRules.canActivate(0.0F, true));
        assertFalse(DeadeyeEnergyRules.canActivate(0.0F, false));
    }

    private static void infiniteEnergyNormalizesEnergyToFull() {
        assertEquals(100.0F, DeadeyeEnergyRules.normalizedEnergy(12.5F, true));
        assertEquals(12.5F, DeadeyeEnergyRules.normalizedEnergy(12.5F, false));
    }

    private static void infiniteEnergyPreventsDrain() {
        assertEquals(100.0F, DeadeyeEnergyRules.energyAfterDrain(35.0F, 12.0F, true));
    }

    private static void ordinaryEnergyStillDrainsAndStopsAtZero() {
        assertEquals(23.0F, DeadeyeEnergyRules.energyAfterDrain(35.0F, 12.0F, false));
        assertEquals(0.0F, DeadeyeEnergyRules.energyAfterDrain(5.0F, 12.0F, false));
    }

    private static void syncIntervalMatchesPacketsPerSecond() {
        assertEquals(20, DeadeyeEnergyRules.syncIntervalTicks(1));
        assertEquals(10, DeadeyeEnergyRules.syncIntervalTicks(2));
        assertEquals(7, DeadeyeEnergyRules.syncIntervalTicks(3));
        assertEquals(5, DeadeyeEnergyRules.syncIntervalTicks(4));
        assertEquals(4, DeadeyeEnergyRules.syncIntervalTicks(5));
        assertEquals(4, DeadeyeEnergyRules.syncIntervalTicks(DeadeyeEnergyRules.DEFAULT_ENERGY_SYNC_RATE));
    }

    private static void midRangeEnergyIsThrottledUntilInterval() {
        int interval = DeadeyeEnergyRules.syncIntervalTicks(5);
        assertFalse(DeadeyeEnergyRules.shouldSyncEnergy(52.5F, 50.0F, 1, interval));
        assertFalse(DeadeyeEnergyRules.shouldSyncEnergy(55.0F, 50.0F, 3, interval));
        assertTrue(DeadeyeEnergyRules.shouldSyncEnergy(60.0F, 50.0F, 4, interval));
    }

    private static void boundaryEnergySyncsImmediately() {
        int interval = DeadeyeEnergyRules.syncIntervalTicks(1);
        assertTrue(DeadeyeEnergyRules.shouldSyncEnergy(0.0F, 2.5F, 1, interval));
        assertTrue(DeadeyeEnergyRules.shouldSyncEnergy(100.0F, 97.5F, 1, interval));
    }

    private static void unchangedEnergyNeverSyncs() {
        int interval = DeadeyeEnergyRules.syncIntervalTicks(5);
        assertFalse(DeadeyeEnergyRules.shouldSyncEnergy(50.0F, 50.0F, 100, interval));
        assertFalse(DeadeyeEnergyRules.shouldSyncEnergy(100.0F, 100.0F, 100, interval));
    }

    private static void leavingFullEnergySyncsImmediately() {
        int interval = DeadeyeEnergyRules.syncIntervalTicks(1);
        assertTrue(DeadeyeEnergyRules.shouldSyncEnergy(97.5F, 100.0F, 1, interval));
    }

    private static void assertTrue(boolean value) {
        if (!value) {
            throw new AssertionError("Expected true");
        }
    }

    private static void assertFalse(boolean value) {
        if (value) {
            throw new AssertionError("Expected false");
        }
    }

    private static void assertEquals(float expected, float actual) {
        if (Math.abs(expected - actual) > 0.0001F) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
