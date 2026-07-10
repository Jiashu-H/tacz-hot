package com.hjsmc.deadeye.client;

public final class DeadeyeConfigUiValuesTest {
    private DeadeyeConfigUiValuesTest() {
    }

    public static void main(String[] args) {
        durationSliderClampsToOneThroughThirty();
        recoveryDelayConvertsToQuarterSecondSteps();
        recoveryDelaySliderClampsToZeroThroughForty();
        recoveryDelayLabelsDoNotContainTrailingZeros();
    }

    private static void durationSliderClampsToOneThroughThirty() {
        assertEquals(1, DeadeyeConfigUiValues.durationSliderValue(0.5D));
        assertEquals(5, DeadeyeConfigUiValues.durationSliderValue(5.0D));
        assertEquals(30, DeadeyeConfigUiValues.durationSliderValue(600.0D));
    }

    private static void recoveryDelayConvertsToQuarterSecondSteps() {
        assertEquals(9, DeadeyeConfigUiValues.recoveryDelaySliderValue(2.25D));
        assertEquals(2.25D, DeadeyeConfigUiValues.recoveryDelaySeconds(9));
    }

    private static void recoveryDelaySliderClampsToZeroThroughForty() {
        assertEquals(0, DeadeyeConfigUiValues.recoveryDelaySliderValue(-1.0D));
        assertEquals(40, DeadeyeConfigUiValues.recoveryDelaySliderValue(20.0D));
        assertEquals(0.0D, DeadeyeConfigUiValues.recoveryDelaySeconds(-4));
        assertEquals(10.0D, DeadeyeConfigUiValues.recoveryDelaySeconds(80));
    }

    private static void recoveryDelayLabelsDoNotContainTrailingZeros() {
        assertEquals("0s", DeadeyeConfigUiValues.recoveryDelayLabel(0));
        assertEquals("0.25s", DeadeyeConfigUiValues.recoveryDelayLabel(1));
        assertEquals("0.5s", DeadeyeConfigUiValues.recoveryDelayLabel(2));
        assertEquals("0.75s", DeadeyeConfigUiValues.recoveryDelayLabel(3));
        assertEquals("1s", DeadeyeConfigUiValues.recoveryDelayLabel(4));
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(double expected, double actual) {
        if (Math.abs(expected - actual) > 0.0001D) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
