package com.hjsmc.deadeye;

import com.hjsmc.deadeye.network.ClientboundDeadeyeStatePacket;
import com.hjsmc.deadeye.network.DeadeyeNetwork;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side time flow control.
 *
 * While at least one player is holding the Deadeye key, every server tick the
 * scheduled time of the next tick ({@code MinecraftServer.nextTickTime},
 * opened via access transformer) is pushed further into the future, so the
 * vanilla pacing loop in {@code MinecraftServer#runServer} waits
 * {@code 50 / rate} ms between ticks instead of 50 ms. That slows the whole
 * server (all dimensions, entities, projectiles, block updates) to
 * {@code 20 * rate} TPS without ever tripping the "Can't keep up!" catch-up
 * logic, because the schedule only ever moves forward.
 *
 * All state is mutated on the server thread only (packet handlers run through
 * {@code enqueueWork}).
 */
@Mod.EventBusSubscriber(modid = DeadeyeMod.MODID)
public final class TimeFlowController {
    private static final long NORMAL_MS_PER_TICK = 50L;

    /** Players currently holding the Deadeye key. Global slow-mo while non-empty. */
    private static final Set<UUID> ACTIVE_HOLDERS = new HashSet<>();
    private static float activeRate = 1.0F;
    /** Fractional milliseconds carried between ticks so odd rates stay exact. */
    private static float carryMs = 0.0F;

    private TimeFlowController() {
    }

    public static boolean isActive() {
        return !ACTIVE_HOLDERS.isEmpty();
    }

    public static boolean isHolding(UUID playerId) {
        return ACTIVE_HOLDERS.contains(playerId);
    }

    /** The rate currently applied (1.0 when inactive). */
    public static float currentRate() {
        return activeRate;
    }

    /** Called on the server thread when a player presses or releases the key. */
    public static void setHolding(ServerPlayer player, boolean holding) {
        if (holding && !DeadeyeEnergyManager.canActivate(player)) {
            return; // out of energy
        }
        boolean wasActive = isActive();
        if (holding) {
            ACTIVE_HOLDERS.add(player.getUUID());
        } else {
            ACTIVE_HOLDERS.remove(player.getUUID());
        }
        if (wasActive == isActive()) {
            return;
        }

        MinecraftServer server = player.serverLevel().getServer();
        if (isActive()) {
            activeRate = (float) (double) DeadeyeConfig.SLOWDOWN_RATE.get();
        } else {
            activeRate = 1.0F;
            carryMs = 0.0F;
            // The last slow tick already scheduled itself far into the future;
            // pull the schedule back so normal speed resumes immediately.
            server.nextTickTime = Math.min(server.nextTickTime, Util.getMillis() + NORMAL_MS_PER_TICK);
        }
        DeadeyeNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                new ClientboundDeadeyeStatePacket(isActive(), activeRate));
    }

    /**
     * Called on the server thread after the configured rate was changed (e.g.
     * via /deadeye rate). If Deadeye is being held right now, the new rate is
     * applied and rebroadcast immediately instead of waiting for the next
     * activation.
     */
    public static void onRateChanged(MinecraftServer server) {
        if (!isActive()) {
            return;
        }
        activeRate = (float) (double) DeadeyeConfig.SLOWDOWN_RATE.get();
        DeadeyeNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                new ClientboundDeadeyeStatePacket(true, activeRate));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isActive()) {
            return;
        }
        float extra = NORMAL_MS_PER_TICK / activeRate - NORMAL_MS_PER_TICK + carryMs;
        long wholeMs = (long) extra;
        carryMs = extra - wholeMs;
        event.getServer().nextTickTime += wholeMs;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DeadeyeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new ClientboundDeadeyeStatePacket(isActive(), activeRate));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            setHolding(player, false);
        }
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        reset();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        reset();
    }

    private static void reset() {
        ACTIVE_HOLDERS.clear();
        activeRate = 1.0F;
        carryMs = 0.0F;
    }
}
