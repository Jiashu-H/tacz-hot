# Wall-Clock Energy Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace integer tick-interval energy synchronization with a monotonic wall-clock fractional-credit limiter for every configured rate from 1 through 20.

**Architecture:** Add a package-private, Forge-independent limiter owned by each server-side player state. The energy manager samples `System.nanoTime()` once per tick, and each limiter converts elapsed nanoseconds into fractional packet credit while boundary updates continue to bypass throttling.

**Tech Stack:** Java 21, Minecraft Forge 1.20.1, the repository's zero-dependency Java test suite, Gradle/IDEA local build caches.

---

### Task 1: Specify Wall-Clock Limiter Behavior With Failing Tests

**Files:**
- Create: `src/test/java/com/hjsmc/deadeye/DeadeyeEnergySyncLimiterTest.java`
- Modify: `src/test/java/com/hjsmc/deadeye/DeadeyeLogicTestSuite.java`

- [ ] **Step 1: Add the limiter regression test class**

Create a test class that calls the wished-for API before production code exists:

```java
package com.hjsmc.deadeye;

public final class DeadeyeEnergySyncLimiterTest {
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

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
        assertFalse(limiter.shouldSync(99.0F, 98.0F,
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
        if (!value) throw new AssertionError("Expected true");
    }

    private static void assertFalse(boolean value) {
        if (value) throw new AssertionError("Expected false");
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
```

- [ ] **Step 2: Register the test in the logic suite**

Add this call to `DeadeyeLogicTestSuite.main`:

```java
DeadeyeEnergySyncLimiterTest.runAll();
```

- [ ] **Step 3: Compile the zero-dependency suite and verify RED**

Run the repository's direct `javac` command including the new test but not a limiter source file.

Expected: compilation fails because `DeadeyeEnergySyncLimiter` does not exist. This proves the new behavior is not implemented by the old tick-interval rules.

### Task 2: Implement the Fractional Wall-Clock Limiter

**Files:**
- Create: `src/main/java/com/hjsmc/deadeye/DeadeyeEnergySyncLimiter.java`
- Test: `src/test/java/com/hjsmc/deadeye/DeadeyeEnergySyncLimiterTest.java`

- [ ] **Step 1: Add the minimal limiter implementation**

```java
package com.hjsmc.deadeye;

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
```

- [ ] **Step 2: Compile and run the direct logic suite to verify GREEN**

Compile `DeadeyeEnergySyncLimiter.java` together with the existing zero-dependency main and test sources, then run `com.hjsmc.deadeye.DeadeyeLogicTestSuite`.

Expected: all limiter and existing logic tests exit with status 0.

### Task 3: Integrate the Limiter and Remove Tick-Interval Rules

**Files:**
- Modify: `src/main/java/com/hjsmc/deadeye/DeadeyeEnergyManager.java`
- Modify: `src/main/java/com/hjsmc/deadeye/DeadeyeEnergyRules.java`
- Modify: `src/test/java/com/hjsmc/deadeye/DeadeyeEnergyRulesTest.java`

- [ ] **Step 1: Replace per-player tick counters with limiter state**

In `DeadeyeEnergyManager.State`, replace `ticksSinceSync` with:

```java
final DeadeyeEnergySyncLimiter syncLimiter = new DeadeyeEnergySyncLimiter();
```

Sample once before the player loop:

```java
long nowNanos = System.nanoTime();
```

Replace interval logic with:

```java
if (state.syncLimiter.shouldSync(
        state.energy, state.lastSyncedEnergy, nowNanos,
        DeadeyeConfig.ENERGY_SYNC_RATE.get())) {
    state.lastSyncedEnergy = state.energy;
    DeadeyeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new ClientboundDeadeyeEnergyPacket(state.energy));
}
```

On login, reset with a fresh monotonic sample:

```java
state.syncLimiter.reset(System.nanoTime());
```

- [ ] **Step 2: Remove obsolete integer interval behavior**

Delete `NORMAL_TICKS_PER_SECOND`, `syncIntervalTicks`, and `shouldSyncEnergy`
from `DeadeyeEnergyRules`. Keep the min/max rate constants, epsilon, and
`isBoundaryEnergy` for the limiter.

Remove the old interval/throttle assertions from `DeadeyeEnergyRulesTest`;
their behavior is fully replaced by `DeadeyeEnergySyncLimiterTest`.

- [ ] **Step 3: Re-run the direct logic suite**

Expected: current-source compilation succeeds and every zero-dependency test exits 0.

### Task 4: Update User-Facing Semantics

**Files:**
- Modify: `README.md`
- Modify: `src/main/java/com/hjsmc/deadeye/DeadeyeConfig.java`
- Modify: `src/main/resources/assets/deadeye/lang/en_us.json`
- Modify: `src/main/resources/assets/deadeye/lang/zh_cn.json`

- [ ] **Step 1: Clarify configuration documentation**

Describe `energySyncRate` as a wall-clock target average and upper bound for
intermediate energy values. State that 0%/100% remain immediate and that the
server sends at most one fresh value per tick, so current TPS is the physical
maximum when it falls below the configured rate.

- [ ] **Step 2: Validate localization JSON**

Parse both language files with PowerShell `ConvertFrom-Json`.

Expected: both files parse without errors.

### Task 5: Verify With Local IDEA/Gradle Assets

**Files:**
- No source changes expected.

- [ ] **Step 1: Run the fresh direct source suite**

Compile all zero-dependency sources into `build/review-test-classes` with the
local JDK 21 and run `DeadeyeLogicTestSuite`.

Expected: exit 0.

- [ ] **Step 2: Run the local project build**

Use the project's Gradle wrapper or the local Gradle 8.8 distribution with
the installed JDK 21 and existing IDEA/Gradle caches.

Run:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'
./gradlew build
```

Expected: `BUILD SUCCESSFUL`. If local plugin metadata is unavailable, report
the exact missing cache artifact and retain the direct source-suite result as
partial verification rather than claiming a complete build.

- [ ] **Step 3: Inspect the final diff**

Run `git diff --check`, `git status --short`, and review the complete diff.

Expected: no whitespace errors and only the limiter, integration, tests, and
documentation files described by this plan are modified.

- [ ] **Step 4: Commit the implementation**

```bash
git add README.md src/main/java/com/hjsmc/deadeye/DeadeyeConfig.java \
  src/main/java/com/hjsmc/deadeye/DeadeyeEnergyManager.java \
  src/main/java/com/hjsmc/deadeye/DeadeyeEnergyRules.java \
  src/main/java/com/hjsmc/deadeye/DeadeyeEnergySyncLimiter.java \
  src/main/resources/assets/deadeye/lang/en_us.json \
  src/main/resources/assets/deadeye/lang/zh_cn.json \
  src/test/java/com/hjsmc/deadeye/DeadeyeEnergyRulesTest.java \
  src/test/java/com/hjsmc/deadeye/DeadeyeEnergySyncLimiterTest.java \
  src/test/java/com/hjsmc/deadeye/DeadeyeLogicTestSuite.java
git commit -m "fix: use wall-clock energy sync rate"
```
