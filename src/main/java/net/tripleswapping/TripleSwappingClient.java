package net.tripleswapping;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.tripleswapping.config.TripleSwappingConfig;
import net.tripleswapping.core.TripleSwapManager;
import net.tripleswapping.gui.TripleSwappingGuiFactory;
import net.tripleswapping.hud.TripleSwappingHudOverlay;
import org.lwjgl.glfw.GLFW;

public class TripleSwappingClient implements ClientModInitializer {
    public static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(
            Identifier.of(TripleSwappingMod.MOD_ID, "main")
    );

    public static final Identifier HUD_LAYER_ID = Identifier.of(TripleSwappingMod.MOD_ID, "hud_overlay");

    public static KeyBinding toggleModKey;
    public static KeyBinding executeSwapKey;
    public static KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        toggleModKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triple_swapping.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KEY_CATEGORY
        ));

        executeSwapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triple_swapping.execute",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.triple_swapping.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KEY_CATEGORY
        ));

        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT,
                HUD_LAYER_ID,
                new TripleSwappingHudOverlay()
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                TripleSwapManager.getInstance().resetPendingState();
                return;
            }

            TripleSwapManager.getInstance().onClientTick(client);

            while (toggleModKey.wasPressed()) {
                TripleSwappingConfig config = TripleSwappingConfig.getInstance();
                config.enabled = !config.enabled;
                TripleSwappingConfig.save();

                String messageKey = config.enabled
                        ? "text.triple_swapping.status_enabled"
                        : "text.triple_swapping.status_disabled";
                client.player.sendMessage(Text.translatable(messageKey), true);
            }

            while (openConfigKey.wasPressed()) {
                client.setScreen(TripleSwappingGuiFactory.createConfigScreen(client.currentScreen));
            }

            TripleSwappingConfig config = TripleSwappingConfig.getInstance();
            if (config.enabled) {
                if (config.triggerMode == TripleSwappingConfig.TriggerMode.KEYBIND) {
                    while (executeSwapKey.wasPressed()) {
                        TripleSwapManager.getInstance().attemptKeybindSwap(client);
                    }
                } else if (config.triggerMode == TripleSwappingConfig.TriggerMode.HOLD_KEY) {
                    if (executeSwapKey.isPressed() && client.player.getAttackCooldownProgress(0.5f) >= 0.9f) {
                        TripleSwapManager.getInstance().attemptKeybindSwap(client);
                    }
                }
            }
        });

        TripleSwappingMod.LOGGER.info("Triple Swapping Client (1.21.11) initialized successfully!");
    }
}
