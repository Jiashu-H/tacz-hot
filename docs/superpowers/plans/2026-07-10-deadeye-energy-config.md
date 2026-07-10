# Deadeye Energy Config Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restrict the Cloth Config duration and recovery-delay controls, add server-authoritative infinite energy, and hide the numeric energy HUD whenever energy is full.

**Architecture:** Keep Forge COMMON config as the source of truth and add one BooleanValue for infinite energy. Extract small package-private pure-Java helpers for UI slider conversion, energy rules, and HUD visibility so behavior can be driven by JUnit tests without launching Minecraft; existing Forge event handlers and overlays delegate to those helpers.

**Tech Stack:** Java 21, Minecraft Forge 1.20.1/47.4.20, Cloth Config 11, Gradle 8.8, JUnit Jupiter 5.10.2.

---

## File map

- Modify `build.gradle`: add JUnit Jupiter and enable the JUnit Platform.
- Create `src/main/java/com/hjsmc/deadeye/client/DeadeyeConfigUiValues.java`: duration and quarter-second slider conversion.
- Create `src/test/java/com/hjsmc/deadeye/client/DeadeyeConfigUiValuesTest.java`: UI conversion tests.
- Create `src/main/java/com/hjsmc/deadeye/DeadeyeEnergyRules.java`: pure infinite-energy activation, normalization, and drain rules.
- Create `src/test/java/com/hjsmc/deadeye/DeadeyeEnergyRulesTest.java`: infinite and ordinary energy behavior tests.
- Modify `src/main/java/com/hjsmc/deadeye/DeadeyeConfig.java`: register `infiniteEnergy`.
- Modify `src/main/java/com/hjsmc/deadeye/DeadeyeEnergyManager.java`: apply infinite-energy rules on the server.
- Modify `src/main/java/com/hjsmc/deadeye/client/DeadeyeClothConfigScreen.java`: use the new slider ranges and add the toggle.
- Create `src/main/java/com/hjsmc/deadeye/client/DeadeyeHudRules.java`: pure full-energy text visibility rule.
- Create `src/test/java/com/hjsmc/deadeye/client/DeadeyeHudRulesTest.java`: HUD visibility tests.
- Modify `src/main/java/com/hjsmc/deadeye/client/DeadeyeVignetteOverlay.java`: delegate energy-text visibility to the tested rule.
- Modify `src/main/resources/assets/deadeye/lang/zh_cn.json`: Chinese toggle text.
- Modify `src/main/resources/assets/deadeye/lang/en_us.json`: English toggle text.
- Modify `README.md`: document the new setting and Cloth UI ranges.

### Task 1: Add the test runtime and quarter-second UI conversion

**Files:**
- Modify: `build.gradle:131-157,215-217`
- Create: `src/test/java/com/hjsmc/deadeye/client/DeadeyeConfigUiValuesTest.java`
- Create: `src/main/java/com/hjsmc/deadeye/client/DeadeyeConfigUiValues.java`

- [ ] **Step 1: Configure JUnit Jupiter**

Add these dependencies at the end of the existing `dependencies` block:

```groovy
    testImplementation platform('org.junit:junit-bom:5.10.2')
    testImplementation 'org.junit.jupiter:junit-jupiter'
```

Add this block before `tasks.withType(JavaCompile)`:

```groovy
tasks.named('test', Test).configure {
    useJUnitPlatform()
}
```

- [ ] **Step 2: Write the failing UI conversion tests**

Create `src/test/java/com/hjsmc/deadeye/client/DeadeyeConfigUiValuesTest.java`:

```java
package com.hjsmc.deadeye.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeadeyeConfigUiValuesTest {
    @Test
    void durationSliderClampsToOneThroughThirty() {
        assertEquals(1, DeadeyeConfigUiValues.durationSliderValue(0.5D));
        assertEquals(5, DeadeyeConfigUiValues.durationSliderValue(5.0D));
        assertEquals(30, DeadeyeConfigUiValues.durationSliderValue(600.0D));
    }

    @Test
    void recoveryDelayConvertsToQuarterSecondSteps() {
        assertEquals(9, DeadeyeConfigUiValues.recoveryDelaySliderValue(2.25D));
        assertEquals(2.25D, DeadeyeConfigUiValues.recoveryDelaySeconds(9), 0.0001D);
    }

    @Test
    void recoveryDelaySliderClampsToZeroThroughForty() {
        assertEquals(0, DeadeyeConfigUiValues.recoveryDelaySliderValue(-1.0D));
        assertEquals(40, DeadeyeConfigUiValues.recoveryDelaySliderValue(20.0D));
        assertEquals(0.0D, DeadeyeConfigUiValues.recoveryDelaySeconds(-4), 0.0001D);
        assertEquals(10.0D, DeadeyeConfigUiValues.recoveryDelaySeconds(80), 0.0001D);
    }

    @Test
    void recoveryDelayLabelsDoNotContainTrailingZeros() {
        assertEquals("0s", DeadeyeConfigUiValues.recoveryDelayLabel(0));
        assertEquals("0.25s", DeadeyeConfigUiValues.recoveryDelayLabel(1));
        assertEquals("0.5s", DeadeyeConfigUiValues.recoveryDelayLabel(2));
        assertEquals("0.75s", DeadeyeConfigUiValues.recoveryDelayLabel(3));
        assertEquals("1s", DeadeyeConfigUiValues.recoveryDelayLabel(4));
    }
}
```

- [ ] **Step 3: Run the test and confirm RED**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.client.DeadeyeConfigUiValuesTest
```

Expected: `compileTestJava` fails because `DeadeyeConfigUiValues` does not exist.

- [ ] **Step 4: Implement the UI conversion helper**

Create `src/main/java/com/hjsmc/deadeye/client/DeadeyeConfigUiValues.java`:

```java
package com.hjsmc.deadeye.client;

final class DeadeyeConfigUiValues {
    static final int DURATION_MIN = 1;
    static final int DURATION_MAX = 30;
    static final int RECOVERY_DELAY_SLIDER_MIN = 0;
    static final int RECOVERY_DELAY_SLIDER_MAX = 40;

    private DeadeyeConfigUiValues() {
    }

    static int durationSliderValue(double seconds) {
        return clamp((int) Math.round(seconds), DURATION_MIN, DURATION_MAX);
    }

    static int recoveryDelaySliderValue(double seconds) {
        return clamp((int) Math.round(seconds * 4.0D),
                RECOVERY_DELAY_SLIDER_MIN, RECOVERY_DELAY_SLIDER_MAX);
    }

    static double recoveryDelaySeconds(int sliderValue) {
        return clamp(sliderValue, RECOVERY_DELAY_SLIDER_MIN, RECOVERY_DELAY_SLIDER_MAX) / 4.0D;
    }

    static String recoveryDelayLabel(int sliderValue) {
        int quarters = clamp(sliderValue, RECOVERY_DELAY_SLIDER_MIN, RECOVERY_DELAY_SLIDER_MAX);
        int wholeSeconds = quarters / 4;
        return switch (quarters % 4) {
            case 1 -> wholeSeconds + ".25s";
            case 2 -> wholeSeconds + ".5s";
            case 3 -> wholeSeconds + ".75s";
            default -> wholeSeconds + "s";
        };
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
```

- [ ] **Step 5: Run the UI conversion tests and confirm GREEN**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.client.DeadeyeConfigUiValuesTest
```

Expected: four tests pass.

- [ ] **Step 6: Commit the tested UI conversion foundation**

```powershell
git add build.gradle src/main/java/com/hjsmc/deadeye/client/DeadeyeConfigUiValues.java src/test/java/com/hjsmc/deadeye/client/DeadeyeConfigUiValuesTest.java
git commit -m "test: add deadeye config UI value coverage"
```

### Task 2: Add tested infinite-energy server rules

**Files:**
- Create: `src/test/java/com/hjsmc/deadeye/DeadeyeEnergyRulesTest.java`
- Create: `src/main/java/com/hjsmc/deadeye/DeadeyeEnergyRules.java`
- Modify: `src/main/java/com/hjsmc/deadeye/DeadeyeConfig.java:8-42`
- Modify: `src/main/java/com/hjsmc/deadeye/DeadeyeEnergyManager.java:18-99`

- [ ] **Step 1: Write the failing energy-rule tests**

Create `src/test/java/com/hjsmc/deadeye/DeadeyeEnergyRulesTest.java`:

```java
package com.hjsmc.deadeye;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeadeyeEnergyRulesTest {
    @Test
    void infiniteEnergyAllowsActivationAtZero() {
        assertTrue(DeadeyeEnergyRules.canActivate(0.0F, true));
        assertFalse(DeadeyeEnergyRules.canActivate(0.0F, false));
    }

    @Test
    void infiniteEnergyConfigDefaultsToDisabled() {
        assertFalse(DeadeyeConfig.INFINITE_ENERGY.getDefault());
    }

    @Test
    void infiniteEnergyNormalizesEnergyToFull() {
        assertEquals(100.0F, DeadeyeEnergyRules.normalizedEnergy(12.5F, true), 0.0001F);
        assertEquals(12.5F, DeadeyeEnergyRules.normalizedEnergy(12.5F, false), 0.0001F);
    }

    @Test
    void infiniteEnergyPreventsDrain() {
        assertEquals(100.0F, DeadeyeEnergyRules.energyAfterDrain(35.0F, 12.0F, true), 0.0001F);
    }

    @Test
    void ordinaryEnergyStillDrainsAndStopsAtZero() {
        assertEquals(23.0F, DeadeyeEnergyRules.energyAfterDrain(35.0F, 12.0F, false), 0.0001F);
        assertEquals(0.0F, DeadeyeEnergyRules.energyAfterDrain(5.0F, 12.0F, false), 0.0001F);
    }
}
```

- [ ] **Step 2: Run the energy tests and confirm RED**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.DeadeyeEnergyRulesTest
```

Expected: `compileTestJava` fails because `DeadeyeEnergyRules` does not exist.

- [ ] **Step 3: Implement the pure energy rules**

Create `src/main/java/com/hjsmc/deadeye/DeadeyeEnergyRules.java`:

```java
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
```

- [ ] **Step 4: Register the common infinite-energy setting**

In `DeadeyeConfig`, declare the new value beside the other energy settings:

```java
    /** Whether Deadeye energy remains permanently full. */
    public static final ForgeConfigSpec.BooleanValue INFINITE_ENERGY;
```

Define it after `ENERGY_DURATION_SECONDS`:

```java
        INFINITE_ENERGY = builder
                .comment(
                        "Keep every player's Deadeye energy at 100% and disable energy consumption.",
                        "使所有玩家的死亡之眼能量保持 100%，并禁用能量消耗。")
                .define("infiniteEnergy", false);
```

- [ ] **Step 5: Delegate the server manager to the tested rules**

Replace the manager constant and activation method with:

```java
    public static final float MAX_ENERGY = DeadeyeEnergyRules.MAX_ENERGY;

    public static boolean canActivate(ServerPlayer player) {
        return DeadeyeEnergyRules.canActivate(
                state(player).energy, DeadeyeConfig.INFINITE_ENERGY.get());
    }
```

In `onServerTick`, read the mode once before iterating players:

```java
        boolean infiniteEnergy = DeadeyeConfig.INFINITE_ENERGY.get();
```

Replace the player energy branch with:

```java
            if (infiniteEnergy) {
                state.ticksSinceUse = 0;
                state.energy = DeadeyeEnergyRules.normalizedEnergy(state.energy, true);
            } else if (TimeFlowController.isHolding(player.getUUID())) {
                state.ticksSinceUse = 0;
                state.energy = DeadeyeEnergyRules.energyAfterDrain(state.energy, drainPerTick, false);
                if (state.energy <= 0.0F) {
                    TimeFlowController.setHolding(player, false); // exhausted
                }
            } else if (state.energy < MAX_ENERGY) {
                if (++state.ticksSinceUse > delayTicks) {
                    state.energy = Math.min(MAX_ENERGY, state.energy + recoveryPerTick);
                }
            }
```

Keep the existing packet synchronization block immediately after this branch so a newly-full energy value is sent to the client.

- [ ] **Step 6: Run the energy tests and confirm GREEN**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.DeadeyeEnergyRulesTest
```

Expected: five tests pass.

- [ ] **Step 7: Run both test classes**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.DeadeyeEnergyRulesTest --tests com.hjsmc.deadeye.client.DeadeyeConfigUiValuesTest
```

Expected: nine tests pass.

- [ ] **Step 8: Commit the infinite-energy gameplay behavior**

```powershell
git add src/main/java/com/hjsmc/deadeye/DeadeyeConfig.java src/main/java/com/hjsmc/deadeye/DeadeyeEnergyManager.java src/main/java/com/hjsmc/deadeye/DeadeyeEnergyRules.java src/test/java/com/hjsmc/deadeye/DeadeyeEnergyRulesTest.java
git commit -m "feat: add server-authoritative infinite energy"
```

### Task 3: Wire the new ranges and toggle into Cloth Config

**Files:**
- Modify: `src/main/java/com/hjsmc/deadeye/client/DeadeyeClothConfigScreen.java:32-74`
- Modify: `src/main/resources/assets/deadeye/lang/zh_cn.json`
- Modify: `src/main/resources/assets/deadeye/lang/en_us.json`

- [ ] **Step 1: Change the duration slider to 1 through 30**

Replace the duration entry with:

```java
        general.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.energyDuration"),
                        DeadeyeConfigUiValues.durationSliderValue(
                                DeadeyeConfig.ENERGY_DURATION_SECONDS.get()),
                        DeadeyeConfigUiValues.DURATION_MIN,
                        DeadeyeConfigUiValues.DURATION_MAX)
                .setDefaultValue(5)
                .setTextGetter(value -> Component.literal(value + "s"))
                .setTooltip(
                        Component.translatable("config.deadeye.energyDuration.tooltip"),
                        Component.translatable("config.deadeye.slowdownRate.tooltip2"))
                .setSaveConsumer(value -> DeadeyeConfig.ENERGY_DURATION_SECONDS.set((double) value))
                .build());
```

- [ ] **Step 2: Add the infinite-energy toggle**

Insert after the duration entry:

```java
        general.addEntry(entries.startBooleanToggle(
                        Component.translatable("config.deadeye.infiniteEnergy"),
                        DeadeyeConfig.INFINITE_ENERGY.get())
                .setDefaultValue(false)
                .setTooltip(
                        Component.translatable("config.deadeye.infiniteEnergy.tooltip"),
                        Component.translatable("config.deadeye.slowdownRate.tooltip2"))
                .setSaveConsumer(DeadeyeConfig.INFINITE_ENERGY::set)
                .build());
```

- [ ] **Step 3: Change recovery delay to quarter-second steps**

Replace the recovery-delay entry with:

```java
        general.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.energyRecoveryDelay"),
                        DeadeyeConfigUiValues.recoveryDelaySliderValue(
                                DeadeyeConfig.ENERGY_RECOVERY_DELAY_SECONDS.get()),
                        DeadeyeConfigUiValues.RECOVERY_DELAY_SLIDER_MIN,
                        DeadeyeConfigUiValues.RECOVERY_DELAY_SLIDER_MAX)
                .setDefaultValue(8)
                .setTextGetter(value -> Component.literal(
                        DeadeyeConfigUiValues.recoveryDelayLabel(value)))
                .setTooltip(
                        Component.translatable("config.deadeye.energyRecoveryDelay.tooltip"),
                        Component.translatable("config.deadeye.slowdownRate.tooltip2"))
                .setSaveConsumer(value -> DeadeyeConfig.ENERGY_RECOVERY_DELAY_SECONDS.set(
                        DeadeyeConfigUiValues.recoveryDelaySeconds(value)))
                .build());
```

The default slider value is `8`, which represents the existing default of `2.0` seconds.

- [ ] **Step 4: Add Chinese translations**

Add these entries after `config.deadeye.energyDuration.tooltip` in `zh_cn.json`:

```json
  "config.deadeye.infiniteEnergy": "无限能量",
  "config.deadeye.infiniteEnergy.tooltip": "开启后所有玩家的能量保持 100%，死亡之眼不会因能量耗尽而停止。",
```

- [ ] **Step 5: Add English translations**

Add these entries after `config.deadeye.energyDuration.tooltip` in `en_us.json`:

```json
  "config.deadeye.infiniteEnergy": "Infinite Energy",
  "config.deadeye.infiniteEnergy.tooltip": "Keep every player's energy at 100% so Deadeye never stops from energy exhaustion.",
```

- [ ] **Step 6: Validate both JSON files**

Run:

```powershell
Get-Content -Raw -Encoding utf8 src/main/resources/assets/deadeye/lang/zh_cn.json | ConvertFrom-Json | Out-Null
Get-Content -Raw -Encoding utf8 src/main/resources/assets/deadeye/lang/en_us.json | ConvertFrom-Json | Out-Null
```

Expected: both commands exit without an error.

- [ ] **Step 7: Run the UI conversion tests and compile main code**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.client.DeadeyeConfigUiValuesTest
```

Expected: four tests pass and `compileJava` succeeds with the Cloth Config changes.

- [ ] **Step 8: Commit the Cloth Config integration**

```powershell
git add src/main/java/com/hjsmc/deadeye/client/DeadeyeClothConfigScreen.java src/main/resources/assets/deadeye/lang/zh_cn.json src/main/resources/assets/deadeye/lang/en_us.json
git commit -m "feat: refine deadeye energy config controls"
```

### Task 4: Hide full energy text in every state

**Files:**
- Create: `src/test/java/com/hjsmc/deadeye/client/DeadeyeHudRulesTest.java`
- Create: `src/main/java/com/hjsmc/deadeye/client/DeadeyeHudRules.java`
- Modify: `src/main/java/com/hjsmc/deadeye/client/DeadeyeVignetteOverlay.java:33-64`

- [ ] **Step 1: Write the failing HUD visibility tests**

Create `src/test/java/com/hjsmc/deadeye/client/DeadeyeHudRulesTest.java`:

```java
package com.hjsmc.deadeye.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeadeyeHudRulesTest {
    @Test
    void fullEnergyTextIsAlwaysHidden() {
        assertFalse(DeadeyeHudRules.shouldShowEnergyText(true, 100.0F));
        assertFalse(DeadeyeHudRules.shouldShowEnergyText(true, 99.95F));
    }

    @Test
    void depletedEnergyTextIsVisibleWhenEnabled() {
        assertTrue(DeadeyeHudRules.shouldShowEnergyText(true, 99.94F));
        assertTrue(DeadeyeHudRules.shouldShowEnergyText(true, 0.0F));
    }

    @Test
    void energyTextSettingStillDisablesTheText() {
        assertFalse(DeadeyeHudRules.shouldShowEnergyText(false, 50.0F));
    }
}
```

- [ ] **Step 2: Run the HUD tests and confirm RED**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.client.DeadeyeHudRulesTest
```

Expected: `compileTestJava` fails because `DeadeyeHudRules` does not exist.

- [ ] **Step 3: Implement the pure HUD rule**

Create `src/main/java/com/hjsmc/deadeye/client/DeadeyeHudRules.java`:

```java
package com.hjsmc.deadeye.client;

final class DeadeyeHudRules {
    private static final float FULL_ENERGY_THRESHOLD = 99.95F;

    private DeadeyeHudRules() {
    }

    static boolean shouldShowEnergyText(boolean enabled, float energy) {
        return enabled && energy < FULL_ENERGY_THRESHOLD;
    }
}
```

- [ ] **Step 4: Use the tested rule in the overlay**

Replace:

```java
        boolean showEnergyText = DeadeyeClientConfig.energyTextEnabled()
                && (energy < 99.95F || showEffects);
```

with:

```java
        boolean showEnergyText = DeadeyeHudRules.shouldShowEnergyText(
                DeadeyeClientConfig.energyTextEnabled(), energy);
```

Update the preceding comment to:

```java
        // Energy text remains visible while recharging, but is hidden at full
        // energy even while the vignette and eye are active.
```

- [ ] **Step 5: Run the HUD tests and confirm GREEN**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.client.DeadeyeHudRulesTest
```

Expected: three tests pass.

- [ ] **Step 6: Run all focused tests**

Run:

```powershell
./gradlew test --tests com.hjsmc.deadeye.DeadeyeEnergyRulesTest --tests com.hjsmc.deadeye.client.DeadeyeConfigUiValuesTest --tests com.hjsmc.deadeye.client.DeadeyeHudRulesTest
```

Expected: twelve tests pass.

- [ ] **Step 7: Commit the HUD behavior**

```powershell
git add src/main/java/com/hjsmc/deadeye/client/DeadeyeHudRules.java src/main/java/com/hjsmc/deadeye/client/DeadeyeVignetteOverlay.java src/test/java/com/hjsmc/deadeye/client/DeadeyeHudRulesTest.java
git commit -m "fix: hide deadeye energy text when full"
```

### Task 5: Update documentation and run full verification

**Files:**
- Modify: `README.md:5-51`

- [ ] **Step 1: Document infinite energy**

Add this row to the common configuration table after `energyDurationSeconds`:

```markdown
| `infiniteEnergy` | 能量始终保持 100%，不会消耗 | `false` | `true` / `false` |
```

Replace the existing energy feature bullet with:

```markdown
- **能量机制**：每位玩家拥有 0～100% 的死亡之眼能量。满能量默认可持续减速 5 秒（按现实时间计，与减速倍率无关）；耗尽后无法减速；停止使用 2 秒后开始恢复。开启 `infiniteEnergy` 后，服务端会将所有玩家的能量保持在 100%，不会消耗，也不会显示能量数值。能量由**服务端权威**计算并同步给客户端显示，多人游戏中客户端无法越权修改
```

- [ ] **Step 2: Document the Cloth Config-only ranges**

After the common configuration table, add:

```markdown
Cloth Config 界面中，持续时间滑条提供整数 1～30 秒；恢复延迟滑条提供 0～10 秒、步进 0.25 秒。TOML 与命令的原有合法范围保持不变。
```

Replace the existing visual-effect feature bullet with:

```markdown
- **屏幕特效**：四周渐晕 + 屏幕中上方的眼睛图标 + 眼睛旁的能量百分比数值，三者同色；渐晕与眼睛同步淡入淡出（实时计时，不受慢动作影响）；能量低于 100% 时显示数值以便观察恢复进度，达到 100% 后始终隐藏数值
```

- [ ] **Step 3: Run every unit test**

Run:

```powershell
./gradlew test
```

Expected: twelve tests pass, with zero failures and zero skipped tests.

- [ ] **Step 4: Run the production build**

Run:

```powershell
./gradlew build
```

Expected: `BUILD SUCCESSFUL`; the reobfuscated JAR is produced under `build/libs/`.

- [ ] **Step 5: Inspect the final diff**

Run:

```powershell
git diff --check
git status --short
git diff HEAD -- README.md
```

Expected: `git diff --check` has no output; status lists only the intended README change before the final documentation commit.

- [ ] **Step 6: Commit the documentation**

```powershell
git add README.md
git commit -m "docs: describe deadeye infinite energy"
```

- [ ] **Step 7: Verify the committed worktree**

Run:

```powershell
git status --short
git log -5 --oneline
```

Expected: the worktree is clean and the implementation commits are visible above the design and plan commits.
