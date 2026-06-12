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
                .setDefaultValue(25)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setTooltip(
                        Component.translatable("config.deadeye.slowdownRate.tooltip1"),
                        Component.translatable("config.deadeye.slowdownRate.tooltip2"))
                .setSaveConsumer(value -> DeadeyeConfig.SLOWDOWN_RATE.set(value / 100.0D))
                .build());
        general.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.energyDuration"),
                        (int) Math.round(DeadeyeConfig.ENERGY_DURATION_SECONDS.get()), 1, 60)
                .setDefaultValue(5)
                .setTextGetter(value -> Component.literal(value + "s"))
                .setTooltip(
                        Component.translatable("config.deadeye.energyDuration.tooltip"),
                        Component.translatable("config.deadeye.slowdownRate.tooltip2"))
                .setSaveConsumer(value -> DeadeyeConfig.ENERGY_DURATION_SECONDS.set((double) value))
                .build());
        general.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.energyRecoveryDelay"),
                        (int) Math.round(DeadeyeConfig.ENERGY_RECOVERY_DELAY_SECONDS.get()), 0, 30)
                .setDefaultValue(2)
                .setTextGetter(value -> Component.literal(value + "s"))
                .setTooltip(
                        Component.translatable("config.deadeye.energyRecoveryDelay.tooltip"),
                        Component.translatable("config.deadeye.slowdownRate.tooltip2"))
                .setSaveConsumer(value -> DeadeyeConfig.ENERGY_RECOVERY_DELAY_SECONDS.set((double) value))
                .build());
        general.addEntry(entries.startDoubleField(
                        Component.translatable("config.deadeye.energyRecoveryRate"),
                        DeadeyeConfig.ENERGY_RECOVERY_PER_TICK.get())
                .setDefaultValue(2.5D)
                .setMin(0.01D)
                .setMax(100.0D)
                .setTooltip(
                        Component.translatable("config.deadeye.energyRecoveryRate.tooltip"),
                        Component.translatable("config.deadeye.slowdownRate.tooltip2"))
                .setSaveConsumer(DeadeyeConfig.ENERGY_RECOVERY_PER_TICK::set)
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
                .setDefaultValue(32)
                .setTextGetter(value -> Component.literal(value + "px"))
                .setTooltip(Component.translatable("config.deadeye.eyeSize.tooltip"))
                .setSaveConsumer(DeadeyeClientConfig.EYE_SIZE::set)
                .build());
        visuals.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.eyeVerticalOffset"),
                        (int) Math.round(DeadeyeClientConfig.eyeVerticalOffset() * 100.0D), 0, 45)
                .setDefaultValue(13)
                .setTextGetter(value -> Component.literal(value + "%"))
                .setTooltip(Component.translatable("config.deadeye.eyeVerticalOffset.tooltip"))
                .setSaveConsumer(value -> DeadeyeClientConfig.EYE_VERTICAL_OFFSET.set(value / 100.0D))
                .build());
        visuals.addEntry(entries.startBooleanToggle(
                        Component.translatable("config.deadeye.energyTextEnabled"),
                        DeadeyeClientConfig.energyTextEnabled())
                .setDefaultValue(true)
                .setTooltip(Component.translatable("config.deadeye.energyTextEnabled.tooltip"))
                .setSaveConsumer(DeadeyeClientConfig.ENERGY_TEXT_ENABLED::set)
                .build());
        visuals.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.energyTextOffsetX"),
                        DeadeyeClientConfig.energyTextOffsetX(), -64, 64)
                .setDefaultValue(4)
                .setTextGetter(value -> Component.literal(value + "px"))
                .setTooltip(Component.translatable("config.deadeye.energyTextOffsetX.tooltip"))
                .setSaveConsumer(DeadeyeClientConfig.ENERGY_TEXT_OFFSET_X::set)
                .build());
        visuals.addEntry(entries.startIntSlider(
                        Component.translatable("config.deadeye.energyTextOffsetY"),
                        DeadeyeClientConfig.energyTextOffsetY(), -64, 64)
                .setDefaultValue(1)
                .setTextGetter(value -> Component.literal(value + "px"))
                .setTooltip(Component.translatable("config.deadeye.energyTextOffsetY.tooltip"))
                .setSaveConsumer(DeadeyeClientConfig.ENERGY_TEXT_OFFSET_Y::set)
                .build());

        return builder.build();
    }
}
