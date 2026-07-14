package com.hjsmc.deadeye;

public final class DeadeyeEnergySyncLimiterTest {
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    private DeadeyeEnergySyncLimiterTest() {
    }

    public static void runAll() {
        allRatesUseWallClockAtTwentyTps();
        fiveTpsPreservesReachableRatesAndCapsHigherRates();
        longPauseDoesNotCreateCatchUpBurst();
        boundariesSyncImmediatelyAndResetCredit();
        unchangedEnergyBanksAtMostOnePromptUpdate();
        resetClearsFractionalCredit();
        backwardAndRepeatedTimeDoNotAddCredit();
    }

    private static void allRatesUseWallClockAtTwentyTps() {
        for (int rate = 1; rate <= 20; rate++) {
            int actual = simulate(rate, 20, 10);
            assertBetween(rate * 10 - 1, rate * 10, actual,
                    "20 TPS rate " + rate);
        }
    }

    private static void fiveTpsPreservesReachableRatesAndCapsHigherRates() {
        for (int rate = 1; rate <= 5; rate++) {
            int actual = simulate(rate, 5, 10);
            assertBetween(rate * 10 - 1, rate * 10, actual,
                    "5 TPS reachable rate " + rate);
        }
        for (int rate = 6; rate <= 20; rate++) {
            assertEquals(50, simulate(rate, 5, 10),
                    "5 TPS physical cap for rate " + rate);
        }
    }

    private static int simulate(int rate, int tps, int seconds) {
        DeadeyeEnergySyncLimiter limiter = new DeadeyeEnergySyncLimiter();
        limiter.reset(0L);
        long step = NANOS_PER_SECOND / tps;
        long now = 0L;
        float current = 40.0F;
        float lastSynced = current;
        int sends = 0;
        for (int tick = 1; tick <= tps * seconds; tick++) {
            now += step;
            current += 0.01F;
            if (limiter.shouldSync(current, lastSynced, now, rate)) {
                sends++;
                lastSynced = current;
            }
        }
        return sends;
    }

    private static void longPauseDoesNotCreateCatchUpBurst() {
        DeadeyeEnergySyncLimiter limiter = new DeadeyeEnergySyncLimiter();
        limiter.reset(0L);
        assertTrue(limiter.shouldSync(51.0F, 50.0F, 5 * NANOS_PER_SECOND, 20));
        assertFalse(limiter.shouldSync(52.0F, 51.0F, 5 * NANOS_PER_SECOND + 1L, 20));
        assertTrue(limiter.shouldSync(53.0F, 51.0F,
                5 * NANOS_PER_SECOND + 50_000_000L, 20));
    }

    private static void boundariesSyncImmediatelyAndResetCredit() {
        DeadeyeEnergySyncLimiter limiter = new DeadeyeEnergySyncLimiter();
        limiter.reset(NANOS_PER_SECOND);
        assertTrue(limiter.shouldSync(0.0F, 50.0F, NANOS_PER_SECOND, 1));
        assertTrue(limiter.shouldSync(1.0F, 0.0F, NANOS_PER_SECOND, 1));
        assertTrue(limiter.shouldSync(100.0F, 99.0F, NANOS_PER_SECOND, 1));
        assertTrue(limiter.shouldSync(99.0F, 100.0F, NANOS_PER_SECOND, 1));
        assertFalse(limiter.shouldSync(98.0F, 99.0F,
                NANOS_PER_SECOND + 1L, 1));
    }

    private static void unchangedEnergyBanksAtMostOnePromptUpdate() {
        DeadeyeEnergySyncLimiter limiter = new DeadeyeEnergySyncLimiter();
        limiter.reset(0L);
        assertFalse(limiter.shouldSync(50.0F, 50.0F, 10 * NANOS_PER_SECOND, 20));
        assertTrue(limiter.shouldSync(51.0F, 50.0F, 10 * NANOS_PER_SECOND, 20));
        assertFalse(limiter.shouldSync(52.0F, 51.0F, 10 * NANOS_PER_SECOND + 1L, 20));
    }

    private static void resetClearsFractionalCredit() {
        DeadeyeEnergySyncLimiter limiter = new DeadeyeEnergySyncLimiter();
        limiter.reset(0L);
        assertFalse(limiter.shouldSync(51.0F, 50.0F, 50_000_000L, 10));
        limiter.reset(50_000_000L);
        assertFalse(limiter.shouldSync(51.0F, 50.0F, 100_000_000L, 10));
        assertTrue(limiter.shouldSync(51.0F, 50.0F, 150_000_000L, 10));
    }

    private static void backwardAndRepeatedTimeDoNotAddCredit() {
        DeadeyeEnergySyncLimiter limiter = new DeadeyeEnergySyncLimiter();
        limiter.reset(NANOS_PER_SECOND);
        assertFalse(limiter.shouldSync(51.0F, 50.0F, 500_000_000L, 20));
        assertFalse(limiter.shouldSync(51.0F, 50.0F, NANOS_PER_SECOND, 20));
        assertTrue(limiter.shouldSync(51.0F, 50.0F,
                NANOS_PER_SECOND + 50_000_000L, 20));
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

    private static void assertEquals(int expected, int actual, String context) {
        if (expected != actual) {
            throw new AssertionError(context + ": expected " + expected + " but got " + actual);
        }
    }

    private static void assertBetween(int min, int max, int actual, String context) {
        if (actual < min || actual > max) {
            throw new AssertionError(context + ": expected " + min + ".." + max + " but got " + actual);
        }
    }
}
