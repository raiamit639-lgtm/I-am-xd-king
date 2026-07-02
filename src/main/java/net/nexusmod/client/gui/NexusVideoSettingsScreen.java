package net.nexusmod.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.nexusmod.client.config.NexusConfig;

/**
 * The main Nexus video settings screen. Lays out every configurable
 * option across four sections (Culling & Distance, Chunk Building,
 * Visual Quality, GUI Behavior) and animates the whole panel sliding
 * up and fading in on open.
 */
public class NexusVideoSettingsScreen extends Screen {

    private final Screen parent;
    private final NexusConfig cfg = NexusConfig.get();

    private float openAnimTicks = 0f;
    private static final float OPEN_ANIM_DURATION = 8f; // ticks

    private static final int PANEL_WIDTH = 360;
    private static final int ROW_HEIGHT = 24;
    private static final int SECTION_GAP = 14;

    public NexusVideoSettingsScreen(Screen parent) {
        super(Text.literal("Nexus Video Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int panelX = (width - PANEL_WIDTH) / 2;
        int y = (height - contentHeight()) / 2;

        y = section(panelX, y, "Culling & Distance");
        y = sliderRow(panelX, y, "Render Distance", 2, 32, cfg.renderDistance,
                v -> Text.literal(v + " chunks"), v -> { cfg.renderDistance = v; cfg.save(); });
        y = toggleRow(panelX, y, "Frustum Culling", () -> cfg.frustumCulling, v -> { cfg.frustumCulling = v; cfg.save(); });
        y = toggleRow(panelX, y, "Occlusion Culling", () -> cfg.occlusionCulling, v -> { cfg.occlusionCulling = v; cfg.save(); });
        y = toggleRow(panelX, y, "Entity Culling", () -> cfg.entityCulling, v -> { cfg.entityCulling = v; cfg.save(); });
        y = toggleRow(panelX, y, "Fog Culling", () -> cfg.fogCulling, v -> { cfg.fogCulling = v; cfg.save(); });
        y += SECTION_GAP;

        y = section(panelX, y, "Chunk Building");
        y = toggleRow(panelX, y, "Async Chunk Rebuild", () -> cfg.asyncChunkRebuild, v -> { cfg.asyncChunkRebuild = v; cfg.save(); });
        y = toggleRow(panelX, y, "Smooth Chunk Loading", () -> cfg.smoothChunkLoading, v -> { cfg.smoothChunkLoading = v; cfg.save(); });
        y = sliderRow(panelX, y, "Builder Threads", 0, 16, cfg.chunkBuilderThreads,
                v -> Text.literal(v == 0 ? "Auto" : String.valueOf(v)), v -> { cfg.chunkBuilderThreads = v; cfg.save(); });
        y += SECTION_GAP;

        y = section(panelX, y, "Visual Quality");
        y = cycleRow(panelX, y, "Graphics Quality", cfg.graphicsQuality,
                v -> { cfg.graphicsQuality = v; cfg.save(); });
        y = toggleRow(panelX, y, "Smooth Lighting", () -> cfg.smoothLighting, v -> { cfg.smoothLighting = v; cfg.save(); });
        y = toggleRow(panelX, y, "Animated Textures", () -> cfg.animatedTextures, v -> { cfg.animatedTextures = v; cfg.save(); });
        y = toggleRow(panelX, y, "Particles", () -> cfg.particles, v -> { cfg.particles = v; cfg.save(); });
        y = sliderRow(panelX, y, "Particle Density", 0, 100, cfg.particleDensity,
                v -> Text.literal(v + "%"), v -> { cfg.particleDensity = v; cfg.save(); });
        y += SECTION_GAP;

        y = section(panelX, y, "GUI Behavior");
        y = toggleRow(panelX, y, "GUI Animations", () -> cfg.guiAnimations, v -> { cfg.guiAnimations = v; cfg.save(); });
        y = sliderRow(panelX, y, "Animation Speed", 50, 200, Math.round(cfg.animationSpeed * 100),
                v -> Text.literal(String.format("%.2fx", v / 100.0)),
                v -> { cfg.animationSpeed = v / 100.0f; cfg.save(); });

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(panelX + PANEL_WIDTH / 2 - 50, y + 10, 100, 20)
                .build());
    }

    private int contentHeight() {
        // 4 sections * header + rows, roughly - kept generous so the panel doesn't clip.
        return 5 + (16 * ROW_HEIGHT) + (4 * SECTION_GAP) + 40;
    }

    private int currentY;

    private int section(int x, int y, String title) {
        currentY = y + 16;
        return currentY;
    }

    private int toggleRow(int x, int y, String label, java.util.function.BooleanSupplier getter, java.util.function.Consumer<Boolean> setter) {
        addDrawableChild(new AnimatedToggleWidget(x, y, PANEL_WIDTH, 16, Text.literal(label), getter, setter));
        return y + ROW_HEIGHT;
    }

    private int sliderRow(int x, int y, String label, int min, int max, int initial,
                           java.util.function.IntFunction<Text> formatter, java.util.function.IntConsumer setter) {
        addDrawableChild(new AnimatedSliderWidget(x, y, PANEL_WIDTH, 16, min, max, initial,
                v -> Text.literal(label + ": ").append(formatter.apply(v)), setter));
        return y + ROW_HEIGHT;
    }

    private int cycleRow(int x, int y, String label, NexusConfig.GraphicsQuality initial,
                          java.util.function.Consumer<NexusConfig.GraphicsQuality> setter) {
        addDrawableChild(CyclingButtonWidget.<NexusConfig.GraphicsQuality>builder(q -> Text.literal(label + ": " + q.name()))
                .values(NexusConfig.GraphicsQuality.values())
                .initially(initial)
                .build(x, y, PANEL_WIDTH, 16, Text.literal(label), (button, value) -> setter.accept(value)));
        return y + ROW_HEIGHT;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (cfg.guiAnimations) {
            openAnimTicks = Math.min(OPEN_ANIM_DURATION, openAnimTicks + delta);
        } else {
            openAnimTicks = OPEN_ANIM_DURATION;
        }

        float t = openAnimTicks / OPEN_ANIM_DURATION;
        float eased = 1 - (1 - t) * (1 - t); // ease-out quad
        int alpha = (int) (eased * 255);
        int slideOffset = (int) ((1 - eased) * 24); // panel slides up 24px into place

        renderBackground(context, mouseX, mouseY, delta);

        int panelX = (width - PANEL_WIDTH) / 2 - 12;
        int panelY = (height - contentHeight()) / 2 - 12 + slideOffset;
        int panelBgColor = (Math.min(180, alpha) << 24) | 0x101014;
        context.fill(panelX, panelY, panelX + PANEL_WIDTH + 24, panelY + contentHeight() + 24, panelBgColor);

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Nexus \u2014 Video Settings").formatted(net.minecraft.util.Formatting.BOLD),
                width / 2, panelY + 6, 0xFFFFFF | (Math.min(255, alpha) << 24));

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        cfg.save();
        if (client != null) {
            client.setScreen(parent);
        }
    }
}
