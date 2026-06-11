package com.hjsmc.deadeye;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * /deadeye rate          - query the current slowdown rate (everyone)
 * /deadeye rate <0.1-1>  - set it (OPs, permission level 2); persists to
 *                          deadeye-common.toml and, if Deadeye is being held
 *                          right now, applies and rebroadcasts immediately.
 */
@Mod.EventBusSubscriber(modid = DeadeyeMod.MODID)
public final class DeadeyeCommands {

    private DeadeyeCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("deadeye")
                .then(Commands.literal("rate")
                        .executes(ctx -> queryRate(ctx.getSource()))
                        .then(Commands.argument("value", DoubleArgumentType.doubleArg(0.1D, 1.0D))
                                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(ctx -> setRate(ctx.getSource(),
                                        DoubleArgumentType.getDouble(ctx, "value"))))));
    }

    private static int queryRate(CommandSourceStack source) {
        int percent = toPercent(DeadeyeConfig.SLOWDOWN_RATE.get());
        source.sendSuccess(() -> Component.translatable("commands.deadeye.rate.query", percent), false);
        return percent;
    }

    private static int setRate(CommandSourceStack source, double value) {
        DeadeyeConfig.SLOWDOWN_RATE.set(value);
        TimeFlowController.onRateChanged(source.getServer());
        int percent = toPercent(value);
        source.sendSuccess(() -> Component.translatable("commands.deadeye.rate.set", percent), true);
        return percent;
    }

    private static int toPercent(double rate) {
        return (int) Math.round(rate * 100.0D);
    }
}
