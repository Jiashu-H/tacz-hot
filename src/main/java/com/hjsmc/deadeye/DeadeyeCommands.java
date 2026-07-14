package com.hjsmc.deadeye;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * /deadeye rate          [0.1-1]    - time speed multiplier
 * /deadeye duration      [0.5-600]  - seconds of slow motion from full energy
 * /deadeye recoverydelay [0-600]    - seconds before energy starts recovering
 * /deadeye recoveryrate  [0.01-100] - energy percent recovered per tick
 * /deadeye syncrate      [1-20]     - mid-range energy display sync packets/s
 * /deadeye energy                   - your current energy
 *
 * Queries are open to everyone; setting values requires permission level 2
 * (OP). Values persist to deadeye-common.toml - the server's file, so clients
 * cannot override gameplay behavior in multiplayer.
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
                                        DoubleArgumentType.getDouble(ctx, "value")))))
                .then(doubleConfigCommand("duration", "seconds", 0.5D, 600.0D,
                        DeadeyeConfig.ENERGY_DURATION_SECONDS, "commands.deadeye.duration"))
                .then(doubleConfigCommand("recoverydelay", "seconds", 0.0D, 600.0D,
                        DeadeyeConfig.ENERGY_RECOVERY_DELAY_SECONDS, "commands.deadeye.recoverydelay"))
                .then(doubleConfigCommand("recoveryrate", "percentPerTick", 0.01D, 100.0D,
                        DeadeyeConfig.ENERGY_RECOVERY_PER_TICK, "commands.deadeye.recoveryrate"))
                .then(Commands.literal("syncrate")
                        .executes(ctx -> querySyncRate(ctx.getSource()))
                        .then(Commands.argument("value", IntegerArgumentType.integer(
                                        DeadeyeEnergyRules.MIN_ENERGY_SYNC_RATE,
                                        DeadeyeEnergyRules.MAX_ENERGY_SYNC_RATE))
                                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .executes(ctx -> setSyncRate(ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "value")))))
                .then(Commands.literal("energy")
                        .executes(ctx -> queryEnergy(ctx.getSource()))));
    }

    /** Builds "<name> [<arg>]" where the query is open and the set is OP-only. */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> doubleConfigCommand(
            String name, String argName, double min, double max,
            ForgeConfigSpec.DoubleValue value, String translationBase) {
        return Commands.literal(name)
                .executes(ctx -> {
                    String formatted = formatNumber(value.get());
                    ctx.getSource().sendSuccess(
                            () -> Component.translatable(translationBase + ".query", formatted), false);
                    return 1;
                })
                .then(Commands.argument(argName, DoubleArgumentType.doubleArg(min, max))
                        .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(ctx -> {
                            double newValue = DoubleArgumentType.getDouble(ctx, argName);
                            value.set(newValue);
                            String formatted = formatNumber(newValue);
                            ctx.getSource().sendSuccess(
                                    () -> Component.translatable(translationBase + ".set", formatted), true);
                            return 1;
                        }));
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

    private static int querySyncRate(CommandSourceStack source) {
        int rate = DeadeyeConfig.ENERGY_SYNC_RATE.get();
        source.sendSuccess(() -> Component.translatable("commands.deadeye.syncrate.query", rate), false);
        return rate;
    }

    private static int setSyncRate(CommandSourceStack source, int value) {
        DeadeyeConfig.ENERGY_SYNC_RATE.set(value);
        source.sendSuccess(() -> Component.translatable("commands.deadeye.syncrate.set", value), true);
        return value;
    }

    private static int queryEnergy(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int percent = Math.round(DeadeyeEnergyManager.energyOf(player));
        source.sendSuccess(() -> Component.translatable("commands.deadeye.energy.query", percent), false);
        return percent;
    }

    private static int toPercent(double rate) {
        return (int) Math.round(rate * 100.0D);
    }

    private static String formatNumber(double value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }
}
