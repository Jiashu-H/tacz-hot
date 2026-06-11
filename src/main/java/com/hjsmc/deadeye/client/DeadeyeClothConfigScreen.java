package com.hjsmc.deadeye.client;

import com.hjsmc.deadeye.DeadeyeClientConfig;
import com.hjsmc.deadeye.DeadeyeConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Cloth Config screen for the mod list "Config" button.
 *
 * "General" edits the gameplay rate in deadeye-common.toml (server-side file
 * is authoritative in multiplayer); "Visual Effects" edits the client-only
 * vignette style in deadeye-client.toml. All save consumers write through the
 * ForgeConfigSpec values, which persist immediately and are picked up live.
 *
 * Only classloaded when the cloth_config mod is present - the extension point
 * in {@link com.hjsmc.deadeye.DeadeyeMod} is registered behind a ModList check.
 */
public final class DeadeyeClothConfigScreen {
    private DeadeyeClothConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.deadeye.title"));
        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.deadeye.category.general"));
        general.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.slowdownRate"),
                        (int) Math.round(DeadeyeConfig.SLOWDOWN_RATE.get() * 100.0D), 10, 100)
                .setDefaultValue(50)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setTooltip(
                        Component.translatable("config.deadeye.slowdownRate.tooltip1"),
                        Component.translatable("config.deadeye.slowdownRate.tooltip2"))
                .setSaveConsumer(value -> DeadeyeConfig.SLOWDOWN_RATE.set(value / 100.0D))
                .build());

        ConfigCategory visuals = builder.getOrCreateCategory(Component.translatable("config.deadeye.category.visuals"));
        visuals.addEntry(entries.startColorField(
                        Component.translatable("config.deadeye.vignetteColor"),
                        DeadeyeClientConfig.colorRgb())
                .setDefaultValue(DeadeyeClientConfig.DEFAULT_COLOR_RGB)
                .setTooltip(Component.translatable("config.deadeye.vignetteColor.tooltip"))
                .setSaveConsumer(DeadeyeClientConfig::setColorRgb)
                .build());
        visuals.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.vignetteOpacity"),
                        (int) Math.round(DeadeyeClientConfig.maxOpacity() * 100.0D), 0, 100)
                .setDefaultValue(30)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setTooltip(Component.translatable("config.deadeye.vignetteOpacity.tooltip"))
                .setSaveConsumer(value -> DeadeyeClientConfig.VIGNETTE_OPACITY.set(value / 100.0D))
                .build());
        visuals.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.vignetteBandWidth"),
                        (int) Math.round(DeadeyeClientConfig.bandFraction() * 100.0D), 5, 50)
                .setDefaultValue(22)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setTooltip(Component.translatable("config.deadeye.vignetteBandWidth.tooltip"))
                .setSaveConsumer(value -> DeadeyeClientConfig.VIGNETTE_BAND_WIDTH.set(value / 100.0D))
                .build());
        visuals.addEntry(entries.startBooleanToggle(
                        Component.translatable("config.deadeye.eyeEnabled"),
                        DeadeyeClientConfig.eyeEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.deadeye.eyeEnabled.tooltip"))
                .setSaveConsumer(DeadeyeClientConfig.EYE_ENABLED::set)
                .build());
        visuals.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.eyeSize"),
                        DeadeyeClientConfig.eyeSize(), 12, 64)
                .setDefaultValue(24)
                .setTextGetter(value -> Component.literal(value + "px"))
                .setTooltip(Component.translatable("config.deadeye.eyeSize.tooltip"))
                .setSaveConsumer(DeadeyeClientConfig.EYE_SIZE::set)
                .build());
        visuals.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.eyeVerticalOffset"),
                        (int) Math.round(DeadeyeClientConfig.eyeVerticalOffset() * 100.0D), 0, 45)
                .setDefaultValue(12)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setTooltip(Component.translatable("config.deadeye.eyeVerticalOffset.tooltip"))
                .setSaveConsumer(value -> DeadeyeClientConfig.EYE_VERTICAL_OFFSET.set(value / 100.0D))
                .build());

        return builder.build();
    }
}
