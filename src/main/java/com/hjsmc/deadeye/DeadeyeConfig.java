package com.hjsmc.deadeye;

import net.minecraftforge.common.ForgeConfigSpec;

public final class DeadeyeConfig {
    public static final ForgeConfigSpec SPEC;

    /** Time speed multiplier applied while Deadeye is active. */
    public static final ForgeConfigSpec.DoubleValue SLOWDOWN_RATE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("deadeye");
        SLOWDOWN_RATE = builder
                .comment(
                        "Time speed multiplier while Deadeye is active.",
                        "0.5 means time flows at half speed, 0.1 is the strongest slow motion, 1.0 disables the slowdown.",
                        "死亡之眼激活时的时间流速倍率：0.5 表示时间以一半速度流逝，0.1 最慢，1.0 等于不减速。")
                .defineInRange("slowdownRate", 0.5D, 0.1D, 1.0D);
        builder.pop();
        SPEC = builder.build();
    }

    private DeadeyeConfig() {
    }
}
