package com.hjsmc.deadeye.client;

import com.hjsmc.deadeye.DeadeyeMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DeadeyeMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DeadeyeClientSetup {
    /**
     * Hold to activate Deadeye. Default X, same as TACZ: Plus. Rebindable in
     * the vanilla Controls screen to any keyboard key or mouse button (mouse
     * buttons are bound by clicking the entry and pressing the button).
     */
    public static final KeyMapping DEADEYE_KEY = new KeyMapping(
            "key.deadeye.activate",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.deadeye");

    private DeadeyeClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(DEADEYE_KEY);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("deadeye_vignette", new DeadeyeVignetteOverlay());
    }
}
