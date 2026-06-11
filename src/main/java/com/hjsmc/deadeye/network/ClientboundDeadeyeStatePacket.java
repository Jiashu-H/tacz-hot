package com.hjsmc.deadeye.network;

import com.hjsmc.deadeye.client.DeadeyeClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Broadcast by the server whenever the global Deadeye state changes (and once
 * on login). Carries the rate so clients never depend on config syncing.
 */
public record ClientboundDeadeyeStatePacket(boolean active, float rate) {

    public static void encode(ClientboundDeadeyeStatePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active());
        buf.writeFloat(msg.rate());
    }

    public static ClientboundDeadeyeStatePacket decode(FriendlyByteBuf buf) {
        return new ClientboundDeadeyeStatePacket(buf.readBoolean(), buf.readFloat());
    }

    public static void handle(ClientboundDeadeyeStatePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Lambda body only executes on the physical client, so the client
            // class is never loaded on a dedicated server.
            if (FMLEnvironment.dist.isClient()) {
                DeadeyeClientState.onStatePacket(msg.active(), msg.rate());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
