package com.hjsmc.deadeye;

import com.hjsmc.deadeye.network.ClientboundDeadeyeEnergyPacket;
import com.hjsmc.deadeye.network.DeadeyeNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative Deadeye energy (0-100%), tracked per player.
 *
 * Holding Deadeye drains energy in proportion to the realtime length of each
 * slowed tick, so a full bar always lasts {@code energyDurationSeconds} of
 * wall-clock slow motion regardless of the slowdown rate. At 0% the hold is
 * force-released and activation is refused. After
 * {@code energyRecoveryDelaySeconds} without use, energy recovers at
 * {@code energyRecoveryPerTick}% per tick.
 *
 * Everything gameplay-relevant happens here on the server; clients only
 * receive display-sync packets, so editing the client-side copy of the common
 * config cannot cheat the mechanic in multiplayer. Mid-range energy is
 * Mid-range energy sync is throttled by {@code energySyncRate}; 0% and 100%
 * still sync immediately.
 */
@Mod.EventBusSubscriber(modid = DeadeyeMod.MODID)
public final class DeadeyeEnergyManager {
    public static final float MAX_ENERGY = DeadeyeEnergyRules.MAX_ENERGY;
    private static final float NORMAL_MS_PER_TICK = 50.0F;

    private static final class State {
        float energy = MAX_ENERGY;
        int ticksSinceUse = Integer.MAX_VALUE / 2;
        float lastSyncedEnergy = MAX_ENERGY;
        int ticksSinceSync = 0;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    private DeadeyeEnergyManager() {
    }

    /** Whether the player has energy left to start (or keep) slowing time. */
    public static boolean canActivate(ServerPlayer player) {
        return DeadeyeEnergyRules.canActivate(
                state(player).energy, DeadeyeConfig.INFINITE_ENERGY.get());
    }

    public static float energyOf(ServerPlayer player) {
        return state(player).energy;
    }

    private static State state(ServerPlayer player) {
        return STATES.computeIfAbsent(player.getUUID(), id -> new State());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        float drainPerTick = drainPerTick();
        float recoveryPerTick = (float) (double) DeadeyeConfig.ENERGY_RECOVERY_PER_TICK.get();
        int delayTicks = (int) Math.round(DeadeyeConfig.ENERGY_RECOVERY_DELAY_SECONDS.get() * 20.0D);
        boolean infiniteEnergy = DeadeyeConfig.INFINITE_ENERGY.get();

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            State state = state(player);
            if (infiniteEnergy) {
                state.ticksSinceUse = 0;
                state.energy = DeadeyeEnergyRules.normalizedEnergy(state.energy, true);
                TimeFlowController.resumeRequestedHolding(player);
            } else if (TimeFlowController.isHolding(player.getUUID())) {
                state.ticksSinceUse = 0;
                state.energy = DeadeyeEnergyRules.energyAfterDrain(state.energy, drainPerTick, false);
                if (state.energy <= 0.0F) {
                    TimeFlowController.suspendForEnergyExhaustion(player);
                }
            } else if (state.energy < MAX_ENERGY) {
                if (++state.ticksSinceUse > delayTicks) {
                    state.energy = Math.min(MAX_ENERGY, state.energy + recoveryPerTick);
                }
            }
            state.ticksSinceSync++;
            int syncInterval = DeadeyeEnergyRules.syncIntervalTicks(DeadeyeConfig.ENERGY_SYNC_RATE.get());
            if (DeadeyeEnergyRules.shouldSyncEnergy(
                    state.energy, state.lastSyncedEnergy, state.ticksSinceSync, syncInterval)) {
                state.lastSyncedEnergy = state.energy;
                state.ticksSinceSync = 0;
                DeadeyeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new ClientboundDeadeyeEnergyPacket(state.energy));
            }
        }
    }

    /**
     * Energy drained per server tick while holding. Uses the realtime length
     * of the (stretched) tick, so the configured duration is wall-clock exact.
     */
    private static float drainPerTick() {
        double durationSeconds = DeadeyeConfig.ENERGY_DURATION_SECONDS.get();
        double tickRealMs = NORMAL_MS_PER_TICK / TimeFlowController.currentRate();
        return (float) (tickRealMs / 1000.0D * (MAX_ENERGY / durationSeconds));
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            State state = state(player);
            state.lastSyncedEnergy = state.energy;
            state.ticksSinceSync = 0;
            DeadeyeNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new ClientboundDeadeyeEnergyPacket(state.energy));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            STATES.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        STATES.clear();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        STATES.clear();
    }
}
