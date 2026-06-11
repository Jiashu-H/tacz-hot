package com.hjsmc.deadeye.client;

import com.hjsmc.deadeye.DeadeyeClientConfig;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * Client-side mirror of the Deadeye state.
 *
 * The client tick rate is paced by {@code Timer.msPerTick} (opened via access
 * transformer). Stretching it to {@code 50 / rate} slows the local tick loop
 * to match the server, while partial-tick interpolation keeps rendering
 * perfectly smooth and per-frame mouse look stays fully responsive - the
 * classic deadeye feel.
 *
 * The vignette fade runs on wall-clock milliseconds on purpose: a tick-based
 * fade would itself be slowed down while Deadeye is active.
 */
public final class DeadeyeClientState {
    private static final float NORMAL_MS_PER_TICK = 50.0F;
    private static final float FADE_IN_MS = 150.0F;
    private static final float FADE_OUT_MS = 350.0F;

    private static boolean active = false;
    /** 0..1 fade animation progress shared by the vignette and the eye icon. */
    private static float fadeProgress = 0.0F;
    private static long lastFadeMs = 0L;

    private DeadeyeClientState() {
    }

    public static void onStatePacket(boolean newActive, float newRate) {
        active = newActive;
        float rate = Mth.clamp(newRate, 0.05F, 1.0F);
        setMsPerTick(active ? NORMAL_MS_PER_TICK / rate : NORMAL_MS_PER_TICK);
    }

    /** Restores normal time flow, e.g. when disconnecting. */
    public static void reset() {
        active = false;
        fadeProgress = 0.0F;
        setMsPerTick(NORMAL_MS_PER_TICK);
    }

    public static boolean isActive() {
        return active;
    }

    private static void setMsPerTick(float msPerTick) {
        Minecraft.getInstance().timer.msPerTick = msPerTick;
    }

    /**
     * Advances the realtime fade and returns the current vignette opacity.
     * Called once per rendered frame by the overlay.
     */
    public static float updateVignetteAlpha() {
        long now = Util.getMillis();
        if (lastFadeMs == 0L) {
            lastFadeMs = now;
        }
        float deltaMs = Math.min(now - lastFadeMs, 100L);
        lastFadeMs = now;

        float target = active ? 1.0F : 0.0F;
        float step = deltaMs / (active ? FADE_IN_MS : FADE_OUT_MS);
        if (fadeProgress < target) {
            fadeProgress = Math.min(fadeProgress + step, target);
        } else {
            fadeProgress = Math.max(fadeProgress - step, target);
        }
        return fadeProgress * DeadeyeClientConfig.maxOpacity();
    }

    /** Current 0..1 fade progress, valid after {@link #updateVignetteAlpha()}. */
    public static float fadeProgress() {
        return fadeProgress;
    }
}
