package com.hjsmc.deadeye;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client-only visual settings (deadeye-client.toml). Deliberately free of any
 * Minecraft client imports: the spec is registered unconditionally in the mod
 * constructor and must stay safe to classload on a dedicated server (where
 * Forge simply never loads CLIENT configs).
 */
public final class DeadeyeClientConfig {
    public static final ForgeConfigSpec SPEC;

    /** Vignette color as RRGGBB hex string, with or without a leading '#'. */
    public static final ForgeConfigSpec.ConfigValue<String> VIGNETTE_COLOR;
    public static final ForgeConfigSpec.DoubleValue VIGNETTE_OPACITY;
    public static final ForgeConfigSpec.DoubleValue VIGNETTE_BAND_WIDTH;

    public static final ForgeConfigSpec.BooleanValue EYE_ENABLED;
    public static final ForgeConfigSpec.IntValue EYE_SIZE;
    public static final ForgeConfigSpec.DoubleValue EYE_VERTICAL_OFFSET;

    public static final ForgeConfigSpec.BooleanValue ENERGY_TEXT_ENABLED;
    public static final ForgeConfigSpec.IntValue ENERGY_TEXT_OFFSET_X;
    public static final ForgeConfigSpec.IntValue ENERGY_TEXT_OFFSET_Y;

    public static final int DEFAULT_COLOR_RGB = 0x1A40E6;

    // Memoized hex parse so the overlay can query per frame for free.
    private static String cachedHex = null;
    private static int cachedRgb = DEFAULT_COLOR_RGB;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("vignette");
        VIGNETTE_COLOR = builder
                .comment(
                        "Color of the screen-edge vignette as RRGGBB hex (a leading # is allowed).",
                        "屏幕边缘渐晕的颜色，RRGGBB 十六进制写法（允许带 # 前缀），例如红色 FF0000、蓝色 1A40E6。")
                .define("color", "1A40E6",
                        value -> value instanceof String s && s.matches("#?[0-9a-fA-F]{6}"));
        VIGNETTE_OPACITY = builder
                .comment(
                        "Maximum opacity of the vignette, 0.0 (invisible) to 1.0 (fully opaque edges).",
                        "渐晕的最大不透明度：0.0 完全不可见，1.0 边缘完全不透明。")
                .defineInRange("opacity", 0.30D, 0.0D, 1.0D);
        VIGNETTE_BAND_WIDTH = builder
                .comment(
                        "Width of each gradient band as a fraction of the screen size, 0.05 to 0.5",
                        "(left/right bands scale with screen width, top/bottom with height; 0.5 makes them meet at the center).",
                        "每条渐变带的宽度，按屏幕尺寸的比例（0.05~0.5）：左右带按屏幕宽度、上下带按屏幕高度计算，0.5 时渐变带在屏幕中心相接。")
                .defineInRange("bandWidth", 0.22D, 0.05D, 0.5D);
        builder.pop();
        builder.push("eye");
        EYE_ENABLED = builder
                .comment(
                        "Show the eye icon in the upper part of the screen while Deadeye is active (tinted with the vignette color).",
                        "死亡之眼激活时是否在屏幕中上方显示眼睛图案（使用渐晕颜色着色）。")
                .define("enabled", true);
        EYE_SIZE = builder
                .comment(
                        "Size of the eye icon in GUI-scaled pixels.",
                        "眼睛图案的大小（GUI 缩放后的像素）。")
                .defineInRange("size", 32, 12, 64);
        EYE_VERTICAL_OFFSET = builder
                .comment(
                        "Vertical position of the eye icon's top edge, as a fraction of the screen height from the top.",
                        "眼睛图案顶边的纵向位置，按屏幕高度的比例从顶部往下计算。")
                .defineInRange("verticalOffset", 0.13D, 0.0D, 0.45D);
        builder.pop();
        builder.push("energy");
        ENERGY_TEXT_ENABLED = builder
                .comment(
                        "Show the energy percentage text beside the eye icon.",
                        "是否在眼睛图案旁显示能量百分比数值。")
                .define("showText", true);
        ENERGY_TEXT_OFFSET_X = builder
                .comment(
                        "Horizontal offset of the energy percentage text from the eye icon's right edge (GUI pixels, negative moves left).",
                        "能量百分比文本相对眼睛图案右缘的横向偏移（GUI 像素，负值向左）。")
                .defineInRange("textOffsetX", 4, -128, 128);
        ENERGY_TEXT_OFFSET_Y = builder
                .comment(
                        "Vertical offset of the energy percentage text from the eye icon's vertical center (GUI pixels, negative moves up).",
                        "能量百分比文本相对眼睛图案竖直中心的纵向偏移（GUI 像素，负值向上）。")
                .defineInRange("textOffsetY", 1, -128, 128);
        builder.pop();
        SPEC = builder.build();
    }

    private DeadeyeClientConfig() {
    }

    public static int colorRgb() {
        String hex = VIGNETTE_COLOR.get();
        if (!hex.equals(cachedHex)) {
            cachedHex = hex;
            try {
                cachedRgb = Integer.parseInt(hex.startsWith("#") ? hex.substring(1) : hex, 16);
            } catch (NumberFormatException e) {
                cachedRgb = DEFAULT_COLOR_RGB;
            }
        }
        return cachedRgb;
    }

    public static void setColorRgb(int rgb) {
        VIGNETTE_COLOR.set(String.format("%06X", rgb & 0xFFFFFF));
    }

    public static float maxOpacity() {
        return (float) (double) VIGNETTE_OPACITY.get();
    }

    public static float bandFraction() {
        return (float) (double) VIGNETTE_BAND_WIDTH.get();
    }

    public static boolean eyeEnabled() {
        return EYE_ENABLED.get();
    }

    public static int eyeSize() {
        return EYE_SIZE.get();
    }

    public static float eyeVerticalOffset() {
        return (float) (double) EYE_VERTICAL_OFFSET.get();
    }

    public static boolean energyTextEnabled() {
        return ENERGY_TEXT_ENABLED.get();
    }

    public static int energyTextOffsetX() {
        return ENERGY_TEXT_OFFSET_X.get();
    }

    public static int energyTextOffsetY() {
        return ENERGY_TEXT_OFFSET_Y.get();
    }
}
