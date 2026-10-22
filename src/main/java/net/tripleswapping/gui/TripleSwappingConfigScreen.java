package net.tripleswapping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.tripleswapping.config.TripleSwappingConfig;

/**
 * Built-in vanilla screen fallback that provides a sleek, modern UI
 * with header/footer accents, contextual tooltips, and interactive setting cyclers.
 */
public class TripleSwappingConfigScreen extends Screen {
    private final Screen parent;
    private final TripleSwappingConfig config;

    public TripleSwappingConfigScreen(Screen parent) {
        super(Text.translatable("text.triple_swapping.title"));
        this.parent = parent;
        this.config = TripleSwappingConfig.get();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 46;
        int buttonWidth = 240;
        int buttonHeight = 20;
        int spacing = 24;

        // Toggle Enabled
        ButtonWidget enabledBtn = ButtonWidget.builder(
                Text.literal("Mod Status: " + (config.enabled ? "§aENABLED§r" : "§cDISABLED§r")),
                button -> {
                    config.enabled = !config.enabled;
                    button.setMessage(Text.literal("Mod Status: " + (config.enabled ? "§aENABLED§r" : "§cDISABLED§r")));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
         .tooltip(Tooltip.of(Text.translatable("tooltip.triple_swapping.enabled")))
         .build();
        this.addDrawableChild(enabledBtn);

        // Trigger Mode
        ButtonWidget triggerBtn = ButtonWidget.builder(
                Text.literal("Trigger: §b" + config.triggerMode.getDisplayName() + "§r"),
                button -> {
                    config.triggerMode = config.triggerMode.next();
                    button.setMessage(Text.literal("Trigger: §b" + config.triggerMode.getDisplayName() + "§r"));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight)
         .tooltip(Tooltip.of(Text.translatable("tooltip.triple_swapping.trigger_mode")))
         .build();
        this.addDrawableChild(triggerBtn);

        // Selection Mode
        ButtonWidget selectionBtn = ButtonWidget.builder(
                Text.literal("Item Selection: §e" + config.selectionMode.getDisplayName() + "§r"),
                button -> {
                    config.selectionMode = config.selectionMode.next();
                    button.setMessage(Text.literal("Item Selection: §e" + config.selectionMode.getDisplayName() + "§r"));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
         .tooltip(Tooltip.of(Text.translatable("tooltip.triple_swapping.slot_mode")))
         .build();
        this.addDrawableChild(selectionBtn);

        // Step 1: Start Item Slot (Attributes)
        ButtonWidget slot1Btn = ButtonWidget.builder(
                Text.literal(String.format("Step 1 (Attributes): §6Slot %d§r", config.startSlot + 1)),
                button -> {
                    config.startSlot = (config.startSlot + 1) % 9;
                    button.setMessage(Text.literal(String.format("Step 1 (Attributes): §6Slot %d§r", config.startSlot + 1)));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight)
         .tooltip(Tooltip.of(Text.translatable("tooltip.triple_swapping.start_slot")))
         .build();
        this.addDrawableChild(slot1Btn);

        // Step 2: Reach Item Slot (Spear range)
        ButtonWidget slot2Btn = ButtonWidget.builder(
                Text.literal(String.format("Step 2 (Reach Range): §bSlot %d§r", config.reachSlot + 1)),
                button -> {
                    config.reachSlot = (config.reachSlot + 1) % 9;
                    button.setMessage(Text.literal(String.format("Step 2 (Reach Range): §bSlot %d§r", config.reachSlot + 1)));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight)
         .tooltip(Tooltip.of(Text.translatable("tooltip.triple_swapping.reach_slot")))
         .build();
        this.addDrawableChild(slot2Btn);

        // Step 3: Attack Item Slot (Mace damage/enchantments)
        ButtonWidget slot3Btn = ButtonWidget.builder(
                Text.literal(String.format("Step 3 (Finishing Hit): §cSlot %d§r", config.attackSlot + 1)),
                button -> {
                    config.attackSlot = (config.attackSlot + 1) % 9;
                    button.setMessage(Text.literal(String.format("Step 3 (Finishing Hit): §cSlot %d§r", config.attackSlot + 1)));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 5, buttonWidth, buttonHeight)
         .tooltip(Tooltip.of(Text.translatable("tooltip.triple_swapping.attack_slot")))
         .build();
        this.addDrawableChild(slot3Btn);

        // Reach Distance
        ButtonWidget reachBtn = ButtonWidget.builder(
                Text.literal(String.format("Reach Range: §d%.1f blocks§r", config.reachDistance)),
                button -> {
                    if (config.reachDistance >= 5.0) {
                        config.reachDistance = 3.5;
                    } else {
                        config.reachDistance += 0.5;
                    }
                    button.setMessage(Text.literal(String.format("Reach Range: §d%.1f blocks§r", config.reachDistance)));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 6, buttonWidth, buttonHeight)
         .tooltip(Tooltip.of(Text.translatable("tooltip.triple_swapping.reach_distance")))
         .build();
        this.addDrawableChild(reachBtn);

        // HUD Indicator Toggle
        ButtonWidget hudBtn = ButtonWidget.builder(
                Text.literal("HUD Overlay: " + (config.hudIndicator ? "§aSHOW§r" : "§cHIDE§r")),
                button -> {
                    config.hudIndicator = !config.hudIndicator;
                    button.setMessage(Text.literal("HUD Overlay: " + (config.hudIndicator ? "§aSHOW§r" : "§cHIDE§r")));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 7, buttonWidth, buttonHeight)
         .tooltip(Tooltip.of(Text.translatable("tooltip.triple_swapping.hud_indicator")))
         .build();
        this.addDrawableChild(hudBtn);

        // Done button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("§aDone & Save§r"),
                button -> this.close()
        ).dimensions(centerX - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void close() {
        config.save();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render custom background darkening
        this.renderBackground(context, mouseX, mouseY, delta);

        // Header & Footer accent bars
        context.fill(0, 0, this.width, 36, 0x88000000);
        context.fill(0, this.height - 38, this.width, this.height, 0x88000000);

        super.render(context, mouseX, mouseY, delta);

        // Title and Subtitle
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFF);
        String sub = String.format("§7Chain: §6S%d§7 -> §bS%d§7 -> §cS%d§r",
                config.startSlot + 1, config.reachSlot + 1, config.attackSlot + 1);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(sub), this.width / 2, 24, 0xAAAAAA);
    }
}
