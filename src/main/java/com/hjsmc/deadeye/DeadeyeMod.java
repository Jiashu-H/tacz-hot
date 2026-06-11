package com.hjsmc.deadeye;

import com.hjsmc.deadeye.client.DeadeyeClothConfigScreen;
import com.hjsmc.deadeye.network.DeadeyeNetwork;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Deadeye: hold a key to slow down the flow of time (RDR2-style slow motion).
 *
 * The slowdown is implemented the same way the Time Stop Clock mod does it:
 * by changing the actual game tick rate. The server stretches its tick
 * interval (see {@link TimeFlowController}) and every client stretches its
 * tick timer (see {@link com.hjsmc.deadeye.client.DeadeyeClientState}), so
 * mobs, projectiles, block updates and player movement all slow down
 * uniformly while mouse aim stays realtime-responsive and rendering remains
 * perfectly smooth thanks to partial-tick interpolation.
 */
@Mod(DeadeyeMod.MODID)
public class DeadeyeMod {
    public static final String MODID = "deadeye";

    public DeadeyeMod(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, DeadeyeConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, DeadeyeClientConfig.SPEC);
        DeadeyeNetwork.register();

        // In-game config screen, available from the mod list when the optional
        // Cloth Config API is installed. The guard keeps cloth (and our screen
        // clss) from ever being classloaded on servers or without cloth.
        if (FMLEnvironment.dist.isClient() && ModList.get().isLoaded("cloth_config")) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, parent) -> DeadeyeClothConfigScreen.create(parent)));
        }
    }
}
