package com.hjsmc.deadeye;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DeadeyeConfig {
    public static final ForgeConfigSpec SPEC;

    /** Time speed multiplier applied while Deadeye is active. */
    public static final ForgeConfigSpec.DoubleValue SLOWDOWN_RATE;
    /** Realtime seconds of slow motion a full energy bar provides. */
    public static final ForgeConfigSpec.DoubleValue ENERGY_DURATION_SECONDS;
    /** Whether Deadeye energy remains permanently full. */
    public static final ForgeConfigSpec.BooleanValue INFINITE_ENERGY;
    /** Seconds without using Deadeye before energy starts recovering. */
    public static final ForgeConfigSpec.DoubleValue ENERGY_RECOVERY_DELAY_SECONDS;
    /** Energy percent recovered per server tick once recovery has started. */
    public static final ForgeConfigSpec.DoubleValue ENERGY_RECOVERY_PER_TICK;
    /** Max mid-range energy display sync packets per second (server → client). */
    public static final ForgeConfigSpec.IntValue ENERGY_SYNC_RATE;

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
        INFINITE_ENERGY = builder
                .comment(
                        "Keep every player's Deadeye energy at 100% and disable energy consumption.",
                        "使所有玩家的死亡之眼能量保持 100%，并禁用能量消耗。")
                .define("infiniteEnergy", false);
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
        ENERGY_SYNC_RATE = builder
                .comment(
                        "Maximum mid-range energy display sync packets per second while energy is changing.",
                        "0% and 100% always sync immediately. Higher values make the HUD smoother; lower values save bandwidth.",
                        "能量在中间值变化时，每秒最多向客户端同步的次数（1~5）。0% 与 100% 始终立即同步。")
                .defineInRange("energySyncRate", 5, 1, 5);
        builder.pop();
        SPEC = builder.build();
    }

    private DeadeyeConfig() {
    }
}
