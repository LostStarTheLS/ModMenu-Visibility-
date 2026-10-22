package net.tripleswapping.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.tripleswapping.config.TripleSwappingConfig;
import net.tripleswapping.core.TripleSwapManager;

public class TripleSwappingHudOverlay implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        TripleSwappingConfig config = TripleSwappingConfig.get();
        if (!config.hudIndicator) {
            return;
        }

        int x = 10;
        int y = 10;

        String statusText = config.enabled ? "§a[ON]§r Triple Swap" : "§c[OFF]§r Triple Swap";
        drawContext.drawTextWithShadow(client.textRenderer, statusText, x, y, 0xFFFFFFFF);

        if (config.enabled) {
            String modeText = "§7Mode: §f" + config.triggerMode.getDisplayName();
            drawContext.drawTextWithShadow(client.textRenderer, modeText, x, y + 10, 0xFFAAAAAA);

            int slot1 = TripleSwapManager.getInstance().resolveSlot(client, 1) + 1;
            int slot2 = TripleSwapManager.getInstance().resolveSlot(client, 2) + 1;
            int slot3 = TripleSwapManager.getInstance().resolveSlot(client, 3) + 1;
            String seqText = String.format("§7Chain: §eS%d §7-> §bS%d §7-> §cS%d§r", slot1, slot2, slot3);
            drawContext.drawTextWithShadow(client.textRenderer, seqText, x, y + 20, 0xFFFFFFFF);
        }
    }
}
