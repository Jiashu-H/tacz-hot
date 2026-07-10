package com.hjsmc.deadeye;

public final class DeadeyeEnergyRulesTest {
    private DeadeyeEnergyRulesTest() {
    }

    public static void main(String[] args) {
        infiniteEnergyAllowsActivationAtZero();
        infiniteEnergyNormalizesEnergyToFull();
        infiniteEnergyPreventsDrain();
        ordinaryEnergyStillDrainsAndStopsAtZero();
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
}
