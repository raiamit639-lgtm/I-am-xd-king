package net.nexusmod.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.nexusmod.client.NexusClient;
import net.nexusmod.client.config.NexusConfig;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * A toggle switch widget with an animated sliding "knob" instead of an
 * instant on/off flip. Animation duration respects the user's configured
 * animation speed, and animations are skipped entirely if GUI animations
 * are disabled in the config.
 */
public class AnimatedToggleWidget extends ClickableWidget {

    private static final int TRACK_ON_COLOR = 0xFF4C9F70;
    private static final int TRACK_OFF_COLOR = 0xFF5A5A5A;
    private static final int KNOB_COLOR = 0xFFF0F0F0;

    private boolean state;
    private float animProgress; // 0.0 = off, 1.0 = on
    private final BooleanSupplier initialState;
    private final Consumer<Boolean> onChange;

    public AnimatedToggleWidget(int x, int y, int width, int height, Text label,
                                 BooleanSupplier initialState, Consumer<Boolean> onChange) {
        super(x, y, width, height, label);
        this.initialState = initialState;
        this.onChange = onChange;
        this.state = initialState.getAsBoolean();
        this.animProgress = state ? 1.0f : 0.0f;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        NexusConfig cfg = NexusConfig.get();

        if (cfg.guiAnimations) {
            float target = state ? 1.0f : 0.0f;
            float speed = 0.20f * Math.max(0.5f, cfg.animationSpeed);
            animProgress = MathHelper.lerp(speed, animProgress, target);
            if (Math.abs(animProgress - target) < 0.002f) {
                animProgress = target;
            }
        } else {
            animProgress = state ? 1.0f : 0.0f;
        }

        int trackColor = lerpColor(TRACK_OFF_COLOR, TRACK_ON_COLOR, animProgress);
        int trackHeight = Math.min(height, 14);
        int trackY = getY() + (height - trackHeight) / 2;
        int trackWidth = 34;
        int trackX = getX();

        context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, trackColor);

        int knobSize = trackHeight - 4;
        int knobTravel = trackWidth - knobSize - 4;
        int knobX = trackX + 2 + Math.round(knobTravel * animProgress);
        int knobY = trackY + 2;
        context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, KNOB_COLOR);

        // Label to the right of the switch
        context.drawText(net.minecraft.client.MinecraftClient.getInstance().textRenderer,
                getMessage(), trackX + trackWidth + 8, getY() + (height - 8) / 2, 0xFFFFFFFF, false);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        state = !state;
        onChange.accept(state);
        NexusClient.client().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f));
    }

    private static int lerpColor(int from, int to, float t) {
        int fa = (from >> 24) & 0xFF, fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int ta = (to >> 24) & 0xFF, tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int a = (int) MathHelper.lerp(t, fa, ta);
        int r = (int) MathHelper.lerp(t, fr, tr);
        int g = (int) MathHelper.lerp(t, fg, tg);
        int b = (int) MathHelper.lerp(t, fb, tb);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void appendClickableNarrations(net.minecraft.client.gui.screen.narration.NarrationMessageBuilder builder) {
        builder.put(net.minecraft.client.gui.screen.narration.NarrationPart.TITLE, getMessage());
    }
}
