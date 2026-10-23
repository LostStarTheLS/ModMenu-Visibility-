package net.tripleswapping.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.tripleswapping.config.TripleSwappingConfig;
import net.tripleswapping.core.RaycastHelper;
import net.tripleswapping.core.TripleSwapManager;

public class TripleSwappingHudOverlay implements HudElement {

    // Modern semi-transparent pill background and accents (32-bit ARGB for 1.21.11 DrawContext)
    private static final int BG_CARD_COLOR = 0xAA101218;
    private static final int BORDER_ACTIVE_COLOR = 0xFF34D399;   // Emerald green
    private static final int BORDER_INACTIVE_COLOR = 0xFFEF4444; // Crimson red
    private static final int BORDER_REACH_COLOR = 0xFF60A5FA;    // Sky blue
    private static final int SLOT_HIGHLIGHT_BG = 0x44FFFFFF;     // Subtle active slot glow
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFF9CA3AF;

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        TripleSwappingConfig config = TripleSwappingConfig.getInstance();
        if (!config.showHudIndicator) {
            return;
        }

        TextRenderer tr = client.textRenderer;
        ClientPlayerEntity player = client.player;

        int padX = 8;
        int padY = 8;

        if (!config.enabled) {
            renderDisabledPill(context, tr, padX, padY);
            return;
        }

        double effectiveReach = RaycastHelper.getEffectiveReachDistance(player, config);

        // Render main active status card with resolved 1.21.11 weapon chain icons
        int cardBottomY = renderMainStatusCard(context, tr, player, config, effectiveReach, padX, padY);

        // Render live target reach card when a combat target is inside the 1.21.11 Spear reach window
        RaycastHelper.TargetResult targetResult = RaycastHelper.findTargetInExtendedReach(client, effectiveReach);
        if (targetResult != null && RaycastHelper.isAttackableTarget(targetResult.entity())) {
            renderTargetReachCard(context, tr, targetResult, config, effectiveReach, padX, cardBottomY + 5);
        }
    }

    /**
     * Compact pill indicator when Triple Swapping is toggled off.
     */
    private void renderDisabledPill(DrawContext context, TextRenderer tr, int x, int y) {
        String label = "Triple Swap: OFF";
        int textWidth = tr.getWidth(label);
        int width = textWidth + 16;
        int height = 18;

        context.fill(x, y, x + width, y + height, BG_CARD_COLOR);
        context.fill(x, y, x + 2, y + height, BORDER_INACTIVE_COLOR);
        context.drawTextWithShadow(tr, Text.literal(label), x + 8, y + 5, 0xFFEF4444);
    }

    /**
     * Renders modern main status pill card displaying:
     * - Active badge & trigger mode
     * - Real-time 1.21.11 weapon item icons for Start -> Spear -> Finish with active hotbar glow
     * - Resolved slot chain & active Spear reach distance
     */
    private int renderMainStatusCard(
            DrawContext context,
            TextRenderer tr,
            ClientPlayerEntity player,
            TripleSwappingConfig config,
            double effectiveReach,
            int x,
            int y
    ) {
        int[] slots = TripleSwapManager.getInstance().resolveSlots(player, config);
        String modeLabel = getCompactTriggerLabel(config.triggerMode);

        int cardWidth = 158;
        int cardHeight = slots != null ? 54 : 36;

        // Card container + emerald left accent stripe
        context.fill(x, y, x + cardWidth, y + cardHeight, BG_CARD_COLOR);
        context.fill(x, y, x + 2, y + cardHeight, BORDER_ACTIVE_COLOR);

        // Header line: Status dot + Title + Mode badge
        context.fill(x + 7, y + 7, x + 11, y + 11, BORDER_ACTIVE_COLOR);
        context.drawTextWithShadow(tr, Text.literal("Triple Swap"), x + 15, y + 5, TEXT_PRIMARY);

        int badgeWidth = tr.getWidth(modeLabel) + 6;
        int badgeX = x + cardWidth - badgeWidth - 5;
        context.fill(badgeX, y + 4, badgeX + badgeWidth, y + 14, 0x3334D399);
        context.drawTextWithShadow(tr, Text.literal(modeLabel), badgeX + 3, y + 5, BORDER_ACTIVE_COLOR);

        if (slots != null) {
            int currentHeldSlot = player.getInventory().getSelectedSlot();
            ItemStack startStack = player.getInventory().getStack(slots[0]);
            ItemStack reachStack = player.getInventory().getStack(slots[1]);
            ItemStack finishStack = player.getInventory().getStack(slots[2]);

            int iconRowY = y + 18;
            int iconStartX = x + 10;

            // Step 1: Start Item (e.g. Sword — start-of-tick attributes)
            renderStepItemBox(context, startStack, iconStartX, iconRowY, currentHeldSlot == slots[0]);
            context.drawTextWithShadow(tr, Text.literal("›"), iconStartX + 21, iconRowY + 4, TEXT_SECONDARY);

            // Step 2: Reach Item (1.21.11 Spear — AttackRangeComponent)
            renderStepItemBox(context, reachStack, iconStartX + 30, iconRowY, currentHeldSlot == slots[1]);
            context.drawTextWithShadow(tr, Text.literal("›"), iconStartX + 51, iconRowY + 4, TEXT_SECONDARY);

            // Step 3: Finishing Item (e.g. Mace — Smash & Enchantments)
            renderStepItemBox(context, finishStack, iconStartX + 60, iconRowY, currentHeldSlot == slots[2]);

            // Subtle footer metadata line: Slot chain + resolved 1.21.11 reach
            String slotInfo = String.format("Slots %d › %d › %d  •  %.1fm", slots[0] + 1, slots[1] + 1, slots[2] + 1, effectiveReach);
            context.drawTextWithShadow(tr, Text.literal(slotInfo), x + 8, y + 41, TEXT_SECONDARY);
        } else {
            context.drawTextWithShadow(tr, Text.literal("⚠ Missing weapon in hotbar"), x + 8, y + 21, 0xFFFBBF24);
        }

        return y + cardHeight;
    }

    /**
     * Renders an individual 18x18 step item icon frame with active highlight when currently held.
     */
    private void renderStepItemBox(DrawContext context, ItemStack stack, int x, int y, boolean isSelected) {
        if (isSelected) {
            context.fill(x - 1, y - 1, x + 17, y + 17, SLOT_HIGHLIGHT_BG);
            context.fill(x - 1, y + 16, x + 17, y + 17, BORDER_ACTIVE_COLOR);
        } else {
            context.fill(x - 1, y - 1, x + 17, y + 17, 0x22000000);
        }

        if (!stack.isEmpty()) {
            context.drawItem(stack, x, y);
        }
    }

    /**
     * Renders a dynamic target reach status pill card when aiming at an attackable entity inside Spear reach.
     */
    private void renderTargetReachCard(
            DrawContext context,
            TextRenderer tr,
            RaycastHelper.TargetResult targetResult,
            TripleSwappingConfig config,
            double effectiveReach,
            int x,
            int y
    ) {
        Entity target = targetResult.entity();
        double dist = targetResult.distance();
        boolean inExtendedReach = dist > config.normalReach;

        int accentColor = inExtendedReach ? BORDER_REACH_COLOR : BORDER_ACTIVE_COLOR;
        int cardWidth = 158;
        int cardHeight = 32;

        context.fill(x, y, x + cardWidth, y + cardHeight, BG_CARD_COLOR);
        context.fill(x, y, x + 2, y + cardHeight, accentColor);

        String targetName = target.getName().getString();
        if (targetName.length() > 14) {
            targetName = targetName.substring(0, 13) + "…";
        }
        String distText = String.format("%.2fm", dist);

        context.drawTextWithShadow(tr, Text.literal(targetName), x + 7, y + 5, TEXT_PRIMARY);

        int distWidth = tr.getWidth(distText);
        context.drawTextWithShadow(tr, Text.literal(distText), x + cardWidth - distWidth - 6, y + 5, accentColor);

        if (target instanceof LivingEntity living && living.getMaxHealth() > 0.0f) {
            float healthRatio = Math.max(0.0f, Math.min(1.0f, living.getHealth() / living.getMaxHealth()));
            int barX = x + 7;
            int barY = y + 18;
            int barWidth = cardWidth - 14;
            int filledWidth = (int) (barWidth * healthRatio);

            context.fill(barX, barY, barX + barWidth, barY + 3, 0x44FFFFFF);
            int barColor = inExtendedReach ? BORDER_REACH_COLOR : BORDER_ACTIVE_COLOR;
            if (filledWidth > 0) {
                context.fill(barX, barY, barX + filledWidth, barY + 3, barColor);
            }

            String rangeTag = inExtendedReach
                    ? String.format("Spear Reach (%.1f–%.1fm)", config.normalReach, effectiveReach)
                    : "Normal Melee (≤" + String.format("%.1fm", config.normalReach) + ")";
            context.drawTextWithShadow(tr, Text.literal(rangeTag), barX, barY + 5, TEXT_SECONDARY);
        } else {
            String rangeStatus = inExtendedReach ? "Spear Extended Reach" : "Normal Melee Range";
            context.drawTextWithShadow(tr, Text.literal(rangeStatus), x + 7, y + 18, accentColor);
        }
    }

    private String getCompactTriggerLabel(TripleSwappingConfig.TriggerMode mode) {
        return switch (mode) {
            case OUT_OF_NORMAL_RANGE -> ">3m AUTO";
            case ON_ATTACK -> "ALWAYS";
            case KEYBIND -> "KEYBIND";
            case HOLD_KEY -> "HOLD";
        };
    }
}
