package com.hjsmc.deadeye.network;

import com.hjsmc.deadeye.client.DeadeyeClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Sent to a player whenever their own Deadeye energy changes (and on login).
 * Display-only on the client; the server remains authoritative.
 */
public record ClientboundDeadeyeEnergyPacket(float energy) {

    public static void encode(ClientboundDeadeyeEnergyPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.energy());
    }

    public static ClientboundDeadeyeEnergyPacket decode(FriendlyByteBuf buf) {
        return new ClientboundDeadeyeEnergyPacket(buf.readFloat());
    }

    public static void handle(ClientboundDeadeyeEnergyPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient()) {
                DeadeyeClientState.setEnergy(msg.energy());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
