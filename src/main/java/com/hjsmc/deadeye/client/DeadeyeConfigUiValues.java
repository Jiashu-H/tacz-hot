package com.hjsmc.deadeye.client;

final class DeadeyeConfigUiValues {
    static final int DURATION_MIN = 1;
    static final int DURATION_MAX = 30;
    static final int RECOVERY_DELAY_SLIDER_MIN = 0;
    static final int RECOVERY_DELAY_SLIDER_MAX = 40;

    private DeadeyeConfigUiValues() {
    }

    static int durationSliderValue(double seconds) {
        return clamp((int) Math.round(seconds), DURATION_MIN, DURATION_MAX);
    }

    static int recoveryDelaySliderValue(double seconds) {
        return clamp((int) Math.round(seconds * 4.0D),
                RECOVERY_DELAY_SLIDER_MIN, RECOVERY_DELAY_SLIDER_MAX);
    }

    static double recoveryDelaySeconds(int sliderValue) {
        return clamp(sliderValue, RECOVERY_DELAY_SLIDER_MIN, RECOVERY_DELAY_SLIDER_MAX) / 4.0D;
    }

    static String recoveryDelayLabel(int sliderValue) {
        int quarters = clamp(sliderValue, RECOVERY_DELAY_SLIDER_MIN, RECOVERY_DELAY_SLIDER_MAX);
        int wholeSeconds = quarters / 4;
        return switch (quarters % 4) {
            case 1 -> wholeSeconds + ".25s";
            case 2 -> wholeSeconds + ".5s";
            case 3 -> wholeSeconds + ".75s";
            default -> wholeSeconds + "s";
        };
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
