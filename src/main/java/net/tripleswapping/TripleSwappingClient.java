package net.tripleswapping;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.tripleswapping.config.TripleSwappingConfig;
import net.tripleswapping.core.TripleSwapManager;
import net.tripleswapping.gui.TripleSwappingConfigScreen;
import net.tripleswapping.hud.TripleSwappingHudOverlay;
import org.lwjgl.glfw.GLFW;

public class TripleSwappingClient implements ClientModInitializer {
    public static final String KEY_CATEGORY = "key.category.triple_swapping";

    public static KeyBinding toggleKey;
    public static KeyBinding executeKey;
    public static KeyBinding configKey;

    @Override
    public void onInitializeClient() {
        TripleSwappingConfig.get(); // Initialize config

        // Register Keybindings
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triple_swapping.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORY
        ));

        executeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triple_swapping.execute",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triple_swapping.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                KEY_CATEGORY
        ));

        // Client Tick Handler for key input
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            TripleSwappingConfig config = TripleSwappingConfig.get();

            // Handle Toggle Key
            while (toggleKey.wasPressed()) {
                config.enabled = !config.enabled;
                config.save();
                client.player.sendMessage(
                        Text.literal("§7[Triple Swapping] §r" + (config.enabled ? "§aEnabled" : "§cDisabled")),
                        true
                );
            }

            // Handle Manual Execute Key
            while (executeKey.wasPressed()) {
                if (config.enabled) {
                    TripleSwapManager.getInstance().tryExecuteTripleSwap(client, false);
                }
            }

            // Handle Hold Key Trigger Mode
            if (config.enabled && config.triggerMode == TripleSwappingConfig.TriggerMode.HOLD_KEY && executeKey.isPressed()) {
                TripleSwapManager.getInstance().tryExecuteTripleSwap(client, false);
            }

            // Handle Open Config GUI Key
            while (configKey.wasPressed()) {
                client.setScreen(new TripleSwappingConfigScreen(client.currentScreen));
            }
        });

        // Register HUD Render Callback
        HudRenderCallback.EVENT.register(new TripleSwappingHudOverlay());

        TripleSwappingMod.LOGGER.info("[{}] Client initialized successfully.", TripleSwappingMod.MOD_NAME);
    }
}
