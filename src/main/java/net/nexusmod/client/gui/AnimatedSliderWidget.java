package net.nexusmod.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.nexusmod.client.config.NexusConfig;

import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * A slider whose fill bar and handle position smoothly ease toward the
 * dragged value rather than snapping instantly, and whose fill color
 * shifts along a gradient based on position. Falls back to instant
 * updates when GUI animations are disabled.
 */
public class AnimatedSliderWidget extends SliderWidget {

    private final int min;
    private final int max;
    private final Function<Integer, Text> labelFormatter;
    private final IntConsumer onChange;
    private double displayedProgress;

    public AnimatedSliderWidget(int x, int y, int width, int height, int min, int max,
                                 int initialValue, Function<Integer, Text> labelFormatter, IntConsumer onChange) {
        super(x, y, width, height, Text.empty(), toProgress(initialValue, min, max));
        this.min = min;
        this.max = max;
        this.labelFormatter = labelFormatter;
        this.onChange = onChange;
        this.displayedProgress = this.value;
        updateMessage();
    }

    private static double toProgress(int value, int min, int max) {
        return MathHelper.clamp((value - min) / (double) (max - min), 0.0, 1.0);
    }

    private int currentIntValue() {
        return min + (int) Math.round(value * (max - min));
    }

    @Override
    protected void updateMessage() {
        setMessage(labelFormatter.apply(currentIntValue()));
    }

    @Override
    protected void applyValue() {
        onChange.accept(currentIntValue());
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        NexusConfig cfg = NexusConfig.get();

        if (cfg.guiAnimations) {
            float speed = 0.25f * Math.max(0.5f, cfg.animationSpeed);
            displayedProgress = MathHelper.lerp(speed, displayedProgress, value);
            if (Math.abs(displayedProgress - value) < 0.001) {
                displayedProgress = value;
            }
        } else {
            displayedProgress = value;
        }

        // Track background
        context.fill(getX(), getY() + height / 2 - 2, getX() + width, getY() + height / 2 + 2, 0xFF3A3A3A);

        // Filled portion up to the animated (eased) progress point
        int filledWidth = (int) (width * displayedProgress);
        int fillColor = gradientColor(displayedProgress);
        context.fill(getX(), getY() + height / 2 - 2, getX() + filledWidth, getY() + height / 2 + 2, fillColor);

        // Handle
        int handleX = getX() + filledWidth - 2;
        context.fill(handleX, getY(), handleX + 4, getY() + height, 0xFFFFFFFF);

        // Label centered above/within the widget
        context.drawCenteredTextWithShadow(
                net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                getMessage(), getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFFFF);
    }

    private static int gradientColor(double t) {
        // Blue (low) -> teal -> green (high), matching the "performance headroom" feel.
        int from = 0xFF4C6EF5; // blue
        int mid = 0xFF4CA0C9;  // teal
        int to = 0xFF4CD97D;   // green
        if (t < 0.5) {
            return lerp(from, mid, t * 2);
        } else {
            return lerp(mid, to, (t - 0.5) * 2);
        }
    }

    private static int lerp(int from, int to, double t) {
        int fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int r = (int) MathHelper.lerp(t, fr, tr);
        int g = (int) MathHelper.lerp(t, fg, tg);
        int b = (int) MathHelper.lerp(t, fb, tb);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
