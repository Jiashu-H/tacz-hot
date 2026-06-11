package com.hjsmc.deadeye;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DeadeyeConfig {
    public static final ForgeConfigSpec SPEC;

    /** Time speed multiplier applied while Deadeye is active. */
    public static final ForgeConfigSpec.DoubleValue SLOWDOWN_RATE;
    /** Realtime seconds of slow motion a full energy bar provides. */
    public static final ForgeConfigSpec.DoubleValue ENERGY_DURATION_SECONDS;
    /** Seconds without using Deadeye before energy starts recovering. */
    public static final ForgeConfigSpec.DoubleValue ENERGY_RECOVERY_DELAY_SECONDS;
    /** Energy percent recovered per server tick once recovery has started. */
    public static final ForgeConfigSpec.DoubleValue ENERGY_RECOVERY_PER_TICK;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("deadeye");
        SLOWDOWN_RATE = builder
                .comment(
                        "Time speed multiplier while Deadeye is active.",
                        "0.5 means time flows at half speed, 0.1 is the strongest slow motion, 1.0 disables the slowdown.",
                        "死亡之眼激活时的时间流速倍率：0.5 表示时间以一半速度流逝，0.1 最慢，1.0 等于不减速。")
                .defineInRange("slowdownRate", 0.25D, 0.1D, 1.0D);
        ENERGY_DURATION_SECONDS = builder
                .comment(
                        "How many realtime seconds of slow motion a full (100%) energy bar provides.",
                        "满能量（100%）可持续减速的秒数（按现实时间计）。")
                .defineInRange("energyDurationSeconds", 5.0D, 0.5D, 600.0D);
        ENERGY_RECOVERY_DELAY_SECONDS = builder
                .comment(
                        "Seconds after the last Deadeye use before energy starts recovering.",
                        "停止使用死亡之眼后，开始恢复能量前的等待秒数。")
                .defineInRange("energyRecoveryDelaySeconds", 2.0D, 0.0D, 600.0D);
        ENERGY_RECOVERY_PER_TICK = builder
                .comment(
                        "Energy percent recovered per server tick once recovery has started (2.5 = full refill in 2 seconds).",
                        "恢复阶段每 tick 回复的能量百分比（2.5 即 2 秒回满）。")
                .defineInRange("energyRecoveryPerTick", 2.5D, 0.01D, 100.0D);
        builder.pop();
        SPEC = builder.build();
    }

    private DeadeyeConfig() {
    }
}
