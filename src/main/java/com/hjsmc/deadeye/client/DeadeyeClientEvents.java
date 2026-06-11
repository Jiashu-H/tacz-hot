package com.hjsmc.deadeye.client;

import com.hjsmc.deadeye.DeadeyeMod;
import com.hjsmc.deadeye.network.DeadeyeNetwork;
import com.hjsmc.deadeye.network.ServerboundDeadeyeHoldPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DeadeyeMod.MODID, value = Dist.CLIENT)
public final class DeadeyeClientEvents {
    private static boolean lastSentHolding = false;

    private DeadeyeClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            lastSentHolding = false;
            return;
        }
        // isDown() honors the IN_GAME conflict context, so opening any screen
        // releases the key and naturally deactivates Deadeye.
        boolean holding = DeadeyeClientSetup.DEADEYE_KEY.isDown();
        if (holding != lastSentHolding) {
            if (DeadeyeNetwork.CHANNEL.isRemotePresent(mc.getConnection().getConnection())) {
                DeadeyeNetwork.CHANNEL.sendToServer(new ServerboundDeadeyeHoldPacket(holding));
            }
            lastSentHolding = holding;
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        lastSentHolding = false;
        DeadeyeClientState.reset();
    }
}
