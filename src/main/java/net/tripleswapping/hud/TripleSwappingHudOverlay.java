package net.tripleswapping.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.tripleswapping.config.TripleSwappingConfig;
import net.tripleswapping.core.RaycastHelper;
import net.tripleswapping.core.TripleSwapManager;

/**
 * Modern HUD Overlay inspired by client-grade HUDs (pill cards, glass styling,
 * animated chain sequence, and dynamic target reach indicator).
 */
public class TripleSwappingHudOverlay implements HudRenderCallback {

    // Dark glass card colors
    private static final int CARD_BG = 0xCC0E111A;
    private static final int CARD_BORDER = 0x55384259;
    private static final int PILL_BG_ENABLED = 0xEE1E824C;
    private static final int PILL_BG_DISABLED = 0xEE96281B;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }

        TripleSwappingConfig config = TripleSwappingConfig.get();
        if (!config.hudIndicator) {
            return;
        }

        TextRenderer tr = client.textRenderer;

        // -----------------------------------------------------------------
        // 1. Watermark & Status Card (Top-Left)
        // -----------------------------------------------------------------
        int cardX = 8;
        int cardY = 8;
        int cardW = 168;
        int cardH = config.enabled ? 50 : 24;

        drawRoundedCard(drawContext, cardX, cardY, cardW, cardH, CARD_BG, CARD_BORDER);

        // Header Title
        drawContext.drawTextWithShadow(tr, Text.literal("§lTRIPLE SWAP§r"), cardX + 8, cardY + 7, 0xFFFFFFFF);

        // Status Pill badge [ON] / [OFF]
        int pillX = cardX + cardW - 36;
        int pillY = cardY + 5;
        int pillW = 28;
        int pillH = 12;
        int pillColor = config.enabled ? PILL_BG_ENABLED : PILL_BG_DISABLED;
        drawRoundedCard(drawContext, pillX, pillY, pillW, pillH, pillColor, 0x44FFFFFF);
        String statusLabel = config.enabled ? "ON" : "OFF";
        int labelOffset = tr.getWidth(statusLabel) / 2;
        drawContext.drawTextWithShadow(tr, Text.literal("§l" + statusLabel), pillX + (pillW / 2) - labelOffset, pillY + 2, 0xFFFFFFFF);

        if (config.enabled) {
            // Mode Subtitle
            String modeStr = "§7Mode: §f" + config.triggerMode.getDisplayName();
            drawContext.drawTextWithShadow(tr, Text.literal(modeStr), cardX + 8, cardY + 21, 0xFFCCCCCC);

            // Active Weapon Chain with Item Slots
            int s1 = TripleSwapManager.getInstance().resolveSlot(client, 1);
            int s2 = TripleSwapManager.getInstance().resolveSlot(client, 2);
            int s3 = TripleSwapManager.getInstance().resolveSlot(client, 3);

            ItemStack stack1 = client.player.getInventory().getStack(s1);
            ItemStack stack2 = client.player.getInventory().getStack(s2);
            ItemStack stack3 = client.player.getInventory().getStack(s3);

            int itemY = cardY + 31;
            drawContext.drawItem(stack1, cardX + 8, itemY);
            drawContext.drawTextWithShadow(tr, Text.literal(String.valueOf(s1 + 1)), cardX + 22, itemY + 5, 0xFFFFCC00);

            drawContext.drawTextWithShadow(tr, Text.literal("§7➔§r"), cardX + 34, itemY + 5, 0xFF888888);

            drawContext.drawItem(stack2, cardX + 46, itemY);
            drawContext.drawTextWithShadow(tr, Text.literal(String.valueOf(s2 + 1)), cardX + 60, itemY + 5, 0xFF55FFFF);

            drawContext.drawTextWithShadow(tr, Text.literal("§7➔§r"), cardX + 72, itemY + 5, 0xFF888888);

            drawContext.drawItem(stack3, cardX + 84, itemY);
            drawContext.drawTextWithShadow(tr, Text.literal(String.valueOf(s3 + 1)), cardX + 98, itemY + 5, 0xFFFF5555);

            String reachSummary = String.format("§6[%.1fm]§r", config.reachDistance);
            drawContext.drawTextWithShadow(tr, Text.literal(reachSummary), cardX + 118, itemY + 5, 0xFFFFAA00);
        }

        // -----------------------------------------------------------------
        // 2. Dynamic Target Reach Card (Similar to TargetHUD in client overlays)
        // -----------------------------------------------------------------
        if (config.enabled) {
            EntityHitResult hitResult = RaycastHelper.raycastEntity(client, config.reachDistance);
            if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
                Entity target = hitResult.getEntity();
                double dist = client.player.getCameraPosVec(1.0f).distanceTo(hitResult.getPos());

                int targetCardW = 150;
                int targetCardH = 34;
                int screenW = client.getWindow().getScaledWidth();
                int screenH = client.getWindow().getScaledHeight();

                // Position slightly below crosshair center
                int targetX = (screenW - targetCardW) / 2;
                int targetY = (screenH / 2) + 32;

                drawRoundedCard(drawContext, targetX, targetY, targetCardW, targetCardH, CARD_BG, 0x8800D2D3);

                // Target Name & Distance
                String targetName = target.getName().getString();
                if (targetName.length() > 14) {
                    targetName = targetName.substring(0, 12) + "..";
                }
                String title = String.format("§f%s §7(%.1fm)", targetName, dist);
                drawContext.drawTextWithShadow(tr, Text.literal(title), targetX + 8, targetY + 6, 0xFFFFFFFF);

                // Reach Status Badge
                boolean isExtendedReach = dist > config.normalReach && dist <= config.reachDistance;
                String badge;
                int badgeColor;
                if (dist <= config.normalReach) {
                    badge = "§7[NORMAL RANGE]";
                    badgeColor = 0xFF888888;
                } else if (isExtendedReach) {
                    badge = "§b⚡ TRIPLE SWAP REACH (+50%)";
                    badgeColor = 0xFF00E5FF;
                } else {
                    badge = "§c[OUT OF REACH]";
                    badgeColor = 0xFFFF5555;
                }
                drawContext.drawTextWithShadow(tr, Text.literal(badge), targetX + 8, targetY + 18, badgeColor);

                // Health bar if LivingEntity
                if (target instanceof LivingEntity living) {
                    float hp = living.getHealth();
                    float maxHp = living.getMaxHealth();
                    float pct = Math.clamp(hp / maxHp, 0.0f, 1.0f);
                    int barW = targetCardW - 16;
                    int barY = targetY + targetCardH - 4;
                    drawContext.fill(targetX + 8, barY, targetX + 8 + barW, barY + 2, 0x66000000);
                    drawContext.fill(targetX + 8, barY, targetX + 8 + (int)(barW * pct), barY + 2, isExtendedReach ? 0xFF00E5FF : 0xFF2ECC71);
                }
            }
        }
    }

    /**
     * Renders a sleek rounded card with corner bevels and border stroke.
     */
    private static void drawRoundedCard(DrawContext context, int x, int y, int w, int h, int bg, int border) {
        // Main body fill
        context.fill(x + 1, y, x + w - 1, y + h, bg);
        context.fill(x, y + 1, x + w, y + h - 1, bg);

        // 1px Border outline
        context.fill(x + 1, y, x + w - 1, y + 1, border);
        context.fill(x + 1, y + h - 1, x + w - 1, y + h, border);
        context.fill(x, y + 1, x + 1, y + h - 1, border);
        context.fill(x + w - 1, y + 1, x + w, y + h - 1, border);
    }
}
