package com.hjsmc.deadeye.network;

import com.hjsmc.deadeye.TimeFlowController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent by the client whenever the Deadeye key is pressed or released. */
public record ServerboundDeadeyeHoldPacket(boolean holding) {

    public static void encode(ServerboundDeadeyeHoldPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.holding());
    }

    public static ServerboundDeadeyeHoldPacket decode(FriendlyByteBuf buf) {
        return new ServerboundDeadeyeHoldPacket(buf.readBoolean());
    }

    public static void handle(ServerboundDeadeyeHoldPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                TimeFlowController.setHolding(sender, msg.holding());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
