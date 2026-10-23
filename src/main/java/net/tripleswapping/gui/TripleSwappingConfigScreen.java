package net.tripleswapping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.tripleswapping.config.TripleSwappingConfig;

public class TripleSwappingConfigScreen extends Screen {
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_SUBTITLE = 0xFFAAAAAA;

    private final Screen parent;
    private final TripleSwappingConfig config;

    public TripleSwappingConfigScreen(Screen parent) {
        super(Text.translatable("text.triple_swapping.title"));
        this.parent = parent;
        this.config = TripleSwappingConfig.getInstance();
    }

    @Override
    protected void init() {
        int colGap = 8;
        int colWidth = 152;
        int totalWidth = colWidth * 2 + colGap;
        int leftColX = (this.width - totalWidth) / 2;
        int rightColX = leftColX + colWidth + colGap;
        int y = 44;
        int rowSpacing = 24;

        // Row 1: Master Toggle | HUD Overlay
        this.addDrawableChild(ButtonWidget.builder(getEnabledText(), button -> {
            config.enabled = !config.enabled;
            button.setMessage(getEnabledText());
        }).dimensions(leftColX, y, colWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getHudText(), button -> {
            config.showHudIndicator = !config.showHudIndicator;
            button.setMessage(getHudText());
        }).dimensions(rightColX, y, colWidth, 20).build());
        y += rowSpacing;

        // Row 2: Trigger Mode | Slot Selection Mode
        this.addDrawableChild(ButtonWidget.builder(getTriggerModeText(), button -> {
            TripleSwappingConfig.TriggerMode[] modes = TripleSwappingConfig.TriggerMode.values();
            config.triggerMode = modes[(config.triggerMode.ordinal() + 1) % modes.length];
            button.setMessage(getTriggerModeText());
        }).dimensions(leftColX, y, colWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getSlotModeText(), button -> {
            TripleSwappingConfig.SlotMode[] modes = TripleSwappingConfig.SlotMode.values();
            config.slotMode = modes[(config.slotMode.ordinal() + 1) % modes.length];
            button.setMessage(getSlotModeText());
        }).dimensions(rightColX, y, colWidth, 20).build());
        y += rowSpacing;

        // Row 3: Step 1 Start Slot | Step 2 Spear Reach Slot
        this.addDrawableChild(ButtonWidget.builder(getStartSlotText(), button -> {
            config.startSlot = (config.startSlot % 9) + 1;
            button.setMessage(getStartSlotText());
        }).dimensions(leftColX, y, colWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getReachSlotText(), button -> {
            config.reachSlot = (config.reachSlot % 9) + 1;
            button.setMessage(getReachSlotText());
        }).dimensions(rightColX, y, colWidth, 20).build());
        y += rowSpacing;

        // Row 4: Step 3 Finishing Slot | Extended Reach Distance
        this.addDrawableChild(ButtonWidget.builder(getAttackSlotText(), button -> {
            config.attackSlot = (config.attackSlot % 9) + 1;
            button.setMessage(getAttackSlotText());
        }).dimensions(leftColX, y, colWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getReachDistText(), button -> {
            double next = Math.round((config.reachDistance + 0.5) * 10.0) / 10.0;
            if (next > 6.5) next = 3.5;
            config.reachDistance = next;
            button.setMessage(getReachDistText());
        }).dimensions(rightColX, y, colWidth, 20).build());
        y += rowSpacing;

        // Row 5: 1.21.11 AttackRangeComponent Toggle | Auto Swap Back to Start Slot
        this.addDrawableChild(ButtonWidget.builder(getComponentRangeText(), button -> {
            config.useItemAttackRangeComponent = !config.useItemAttackRangeComponent;
            button.setMessage(getComponentRangeText());
        }).dimensions(leftColX, y, colWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(getAutoSwapBackText(), button -> {
            config.autoSwapBack = !config.autoSwapBack;
            button.setMessage(getAutoSwapBackText());
        }).dimensions(rightColX, y, colWidth, 20).build());

        // Bottom Footer Buttons
        int footerY = Math.max(y + 32, this.height - 28);
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("text.triple_swapping.reset"), button -> {
            config.resetToDefaults();
            this.clearAndInit();
        }).dimensions(leftColX, footerY, colWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> close())
                .dimensions(rightColX, footerY, colWidth, 20)
                .build());
    }

    private Text getEnabledText() {
        return Text.literal("Triple Swap: " + (config.enabled ? "§aON" : "§cOFF"));
    }

    private Text getHudText() {
        return Text.literal("HUD Overlay: " + (config.showHudIndicator ? "§aON" : "§cOFF"));
    }

    private Text getTriggerModeText() {
        String label = switch (config.triggerMode) {
            case ON_ATTACK -> "On Attack";
            case KEYBIND -> "Keybind";
            case HOLD_KEY -> "Hold Key";
            case OUT_OF_NORMAL_RANGE -> ">3m Auto";
        };
        return Text.literal("Trigger: §e" + label);
    }

    private Text getSlotModeText() {
        String label = config.slotMode == TripleSwappingConfig.SlotMode.HOTBAR_SLOTS ? "Hotbar Slots" : "Auto Item ID";
        return Text.literal("Selection: §b" + label);
    }

    private Text getStartSlotText() {
        return Text.literal("Step 1 (Start): §aSlot " + config.startSlot);
    }

    private Text getReachSlotText() {
        return Text.literal("Step 2 (Spear): §bSlot " + config.reachSlot);
    }

    private Text getAttackSlotText() {
        return Text.literal("Step 3 (Finish): §6Slot " + config.attackSlot);
    }

    private Text getReachDistText() {
        return Text.literal(String.format("Reach Cap: §d%.1f blocks", config.reachDistance));
    }

    private Text getComponentRangeText() {
        return Text.literal("Spear Range Data: " + (config.useItemAttackRangeComponent ? "§aON" : "§cOFF"));
    }

    private Text getAutoSwapBackText() {
        return Text.literal("Auto Return Slot: " + (config.autoSwapBack ? "§aON" : "§cOFF"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        // Note: In Minecraft 1.21.6–1.21.11, super.render(...) automatically invokes renderBackground(...) once per frame.
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 14, COLOR_WHITE);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Sword Attributes › 1.21.11 Spear Reach (4.5m) › Finishing Impact"),
                this.width / 2,
                27,
                COLOR_SUBTITLE
        );
    }

    @Override
    public void close() {
        TripleSwappingConfig.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
