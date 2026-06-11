package com.hjsmc.deadeye.network;

import com.hjsmc.deadeye.DeadeyeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class DeadeyeNetwork {
    private static final String PROTOCOL_VERSION = "2";

    /**
     * The server requires matching clients (an unmodded client would desync
     * badly while time is slowed), but a modded client may still join
     * unmodded/vanilla servers - Deadeye simply stays inactive there.
     */
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(DeadeyeMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            remote -> PROTOCOL_VERSION.equals(remote)
                    || NetworkRegistry.ABSENT.version().equals(remote)
                    || NetworkRegistry.ACCEPTVANILLA.equals(remote),
            PROTOCOL_VERSION::equals);

    private DeadeyeNetwork() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ServerboundDeadeyeHoldPacket.class,
                ServerboundDeadeyeHoldPacket::encode,
                ServerboundDeadeyeHoldPacket::decode,
                ServerboundDeadeyeHoldPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, ClientboundDeadeyeStatePacket.class,
                ClientboundDeadeyeStatePacket::encode,
                ClientboundDeadeyeStatePacket::decode,
                ClientboundDeadeyeStatePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(id++, ClientboundDeadeyeEnergyPacket.class,
                ClientboundDeadeyeEnergyPacket::encode,
                ClientboundDeadeyeEnergyPacket::decode,
                ClientboundDeadeyeEnergyPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
