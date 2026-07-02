package net.nexusmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.nexusmod.client.config.NexusConfig;
import net.nexusmod.client.gui.NexusVideoSettingsScreen;
import org.lwjgl.glfw.GLFW;

/**
 * Entrypoint for the Nexus client mod. Registers the config, the
 * keybinding used to open the video settings panel, and hooks into
 * the client tick loop for anything that needs polling (e.g. GUI
 * animation state).
 */
public class NexusClient implements ClientModInitializer {

    public static final String MOD_ID = "nexus";

    private static KeyBinding openSettingsKey;

    @Override
    public void onInitializeClient() {
        // Load config eagerly so defaults are written to disk on first run.
        NexusConfig.get();

        openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.nexus.open_settings",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.nexus.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openSettingsKey.wasPressed() && client.currentScreen == null) {
                client.setScreen(new NexusVideoSettingsScreen(null));
            }
        });

        System.out.println("[Nexus] Initialized. Press N in-game to open video settings.");
    }

    public static MinecraftClient client() {
        return MinecraftClient.getInstance();
    }
}
