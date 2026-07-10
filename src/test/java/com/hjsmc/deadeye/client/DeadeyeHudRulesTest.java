package com.hjsmc.deadeye.client;

public final class DeadeyeHudRulesTest {
    private DeadeyeHudRulesTest() {
    }

    public static void main(String[] args) {
        fullEnergyTextIsAlwaysHidden();
        depletedEnergyTextIsVisibleWhenEnabled();
        energyTextSettingStillDisablesTheText();
    }

    private static void fullEnergyTextIsAlwaysHidden() {
        assertFalse(DeadeyeHudRules.shouldShowEnergyText(true, 100.0F));
        assertFalse(DeadeyeHudRules.shouldShowEnergyText(true, 99.50F));
        assertFalse(DeadeyeHudRules.shouldShowEnergyText(true, 99.94F));
        assertFalse(DeadeyeHudRules.shouldShowEnergyText(true, 99.95F));
    }

    private static void depletedEnergyTextIsVisibleWhenEnabled() {
        assertTrue(DeadeyeHudRules.shouldShowEnergyText(true, 99.49F));
        assertTrue(DeadeyeHudRules.shouldShowEnergyText(true, 0.0F));
    }

    private static void energyTextSettingStillDisablesTheText() {
        assertFalse(DeadeyeHudRules.shouldShowEnergyText(false, 50.0F));
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
}
