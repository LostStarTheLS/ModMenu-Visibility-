package net.tripleswapping.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.tripleswapping.config.TripleSwappingConfig;

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
        int startY = 40;
        int buttonWidth = 220;
        int buttonHeight = 20;
        int spacing = 24;

        // Toggle Enabled
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Mod Status: " + (config.enabled ? "§aENABLED§r" : "§cDISABLED§r")),
                button -> {
                    config.enabled = !config.enabled;
                    button.setMessage(Text.literal("Mod Status: " + (config.enabled ? "§aENABLED§r" : "§cDISABLED§r")));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight).build());

        // Trigger Mode
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Trigger: §b" + config.triggerMode.getDisplayName() + "§r"),
                button -> {
                    config.triggerMode = config.triggerMode.next();
                    button.setMessage(Text.literal("Trigger: §b" + config.triggerMode.getDisplayName() + "§r"));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight).build());

        // Selection Mode
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Item Selection: §e" + config.selectionMode.getDisplayName() + "§r"),
                button -> {
                    config.selectionMode = config.selectionMode.next();
                    button.setMessage(Text.literal("Item Selection: §e" + config.selectionMode.getDisplayName() + "§r"));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight).build());

        // Step 1: Start Item Slot (Attributes)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Step 1 (Attributes): Hotbar Slot " + (config.startSlot + 1)),
                button -> {
                    config.startSlot = (config.startSlot + 1) % 9;
                    button.setMessage(Text.literal("Step 1 (Attributes): Hotbar Slot " + (config.startSlot + 1)));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight).build());

        // Step 2: Reach Item Slot (Spear range)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Step 2 (Reach Range): Hotbar Slot " + (config.reachSlot + 1)),
                button -> {
                    config.reachSlot = (config.reachSlot + 1) % 9;
                    button.setMessage(Text.literal("Step 2 (Reach Range): Hotbar Slot " + (config.reachSlot + 1)));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight).build());

        // Step 3: Attack Item Slot (Mace damage/enchantments)
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Step 3 (Damage Weapon): Hotbar Slot " + (config.attackSlot + 1)),
                button -> {
                    config.attackSlot = (config.attackSlot + 1) % 9;
                    button.setMessage(Text.literal("Step 3 (Damage Weapon): Hotbar Slot " + (config.attackSlot + 1)));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 5, buttonWidth, buttonHeight).build());

        // Reach Distance
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal(String.format("Reach Distance: §6%.1f blocks§r", config.reachDistance)),
                button -> {
                    if (config.reachDistance >= 5.0) {
                        config.reachDistance = 3.5;
                    } else {
                        config.reachDistance += 0.5;
                    }
                    button.setMessage(Text.literal(String.format("Reach Distance: §6%.1f blocks§r", config.reachDistance)));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 6, buttonWidth, buttonHeight).build());

        // HUD Indicator Toggle
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("HUD Overlay: " + (config.hudIndicator ? "§aSHOW§r" : "§cHIDE§r")),
                button -> {
                    config.hudIndicator = !config.hudIndicator;
                    button.setMessage(Text.literal("HUD Overlay: " + (config.hudIndicator ? "§aSHOW§r" : "§cHIDE§r")));
                    config.save();
                }
        ).dimensions(centerX - buttonWidth / 2, startY + spacing * 7, buttonWidth, buttonHeight).build());

        // Done button
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Done"),
                button -> this.close()
        ).dimensions(centerX - 100, startY + spacing * 8 + 8, 200, buttonHeight).build());
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
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}
