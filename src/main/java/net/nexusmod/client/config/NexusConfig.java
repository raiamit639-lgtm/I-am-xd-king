package net.nexusmod.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Central configuration object for Nexus. Holds every value the video
 * settings GUI reads from and writes to, and handles loading/saving
 * a JSON config file in the Minecraft config directory.
 */
public class NexusConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static NexusConfig instance;

    // --- Rendering distance & culling ---
    public int renderDistance = 12;          // chunks, 2-32
    public boolean frustumCulling = true;     // skip chunks outside camera view
    public boolean occlusionCulling = true;   // skip chunks hidden behind terrain
    public boolean entityCulling = true;      // skip rendering off-screen entities
    public boolean fogCulling = true;         // skip geometry hidden by fog

    // --- Chunk build / threading ---
    public int chunkBuilderThreads = 0;       // 0 = auto-detect from core count
    public boolean asyncChunkRebuild = true;  // batch chunk mesh rebuilds off the main thread
    public boolean smoothChunkLoading = true; // fade chunks in as they finish building

    // --- Visual quality ---
    public GraphicsQuality graphicsQuality = GraphicsQuality.FANCY;
    public boolean smoothLighting = true;
    public boolean animatedTextures = true;
    public boolean particles = true;
    public int particleDensity = 100;         // percent, 0-100

    // --- GUI behavior ---
    public boolean guiAnimations = true;      // enable panel/slider transition animations
    public float animationSpeed = 1.0f;       // multiplier, 0.5-2.0

    public enum GraphicsQuality {
        FAST, FANCY, FABULOUS
    }

    public static NexusConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    private static Path configPath() {
        return Path.of("config", "nexus.json");
    }

    private static NexusConfig load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                NexusConfig loaded = GSON.fromJson(reader, NexusConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                System.err.println("[Nexus] Failed to read config, using defaults: " + e.getMessage());
            }
        }
        return new NexusConfig();
    }

    public void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[Nexus] Failed to save config: " + e.getMessage());
        }
    }
}
