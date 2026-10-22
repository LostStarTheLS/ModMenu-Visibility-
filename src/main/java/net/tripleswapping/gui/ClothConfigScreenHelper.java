package net.tripleswapping.gui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.tripleswapping.config.TripleSwappingConfig;

/**
 * Builds a modern, polished configuration screen using the Cloth Config library.
 * This public library provides categories, smooth sliders, tooltips, validation,
 * and clean UI styling native to modern Minecraft modding.
 */
public class ClothConfigScreenHelper {

    public static Screen createConfigScreen(Screen parent) {
        TripleSwappingConfig config = TripleSwappingConfig.get();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("text.triple_swapping.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // ---------------------------------------------------------
        // Category 1: General & Combat Trigger
        // ---------------------------------------------------------
        ConfigCategory generalCategory = builder.getOrCreateCategory(
                Text.translatable("category.triple_swapping.general")
        );

        // Master Toggle
        generalCategory.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("text.triple_swapping.enabled"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.triple_swapping.enabled"))
                .setSaveConsumer(newValue -> config.enabled = newValue)
                .build());

        // Trigger Mode Selector
        generalCategory.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("text.triple_swapping.trigger_mode"),
                        TripleSwappingConfig.TriggerMode.class,
                        config.triggerMode)
                .setDefaultValue(TripleSwappingConfig.TriggerMode.OUT_OF_RANGE_ONLY)
                .setTooltip(Text.translatable("tooltip.triple_swapping.trigger_mode"))
                .setSaveConsumer(newValue -> config.triggerMode = newValue)
                .build());

        // Extended Reach Distance Slider (3.0 to 6.0 blocks)
        generalCategory.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("text.triple_swapping.reach_distance"),
                        config.reachDistance)
                .setDefaultValue(4.5)
                .setMin(3.0)
                .setMax(6.0)
                .setTooltip(Text.translatable("tooltip.triple_swapping.reach_distance"))
                .setSaveConsumer(newValue -> config.reachDistance = newValue)
                .build());

        // Client Transition Delay Window (ms)
        generalCategory.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.client_delay"),
                        config.clientDelayMs, 10, 100)
                .setDefaultValue(50)
                .setTooltip(Text.translatable("tooltip.triple_swapping.client_delay"))
                .setSaveConsumer(newValue -> config.clientDelayMs = newValue)
                .build());

        // ---------------------------------------------------------
        // Category 2: Weapon & Slot Setup
        // ---------------------------------------------------------
        ConfigCategory weaponCategory = builder.getOrCreateCategory(
                Text.translatable("category.triple_swapping.weapons")
        );

        // Selection Mode (Hotbar Slot vs Item ID)
        weaponCategory.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("text.triple_swapping.slot_mode"),
                        TripleSwappingConfig.SelectionMode.class,
                        config.selectionMode)
                .setDefaultValue(TripleSwappingConfig.SelectionMode.BY_HOTBAR_SLOT)
                .setTooltip(Text.translatable("tooltip.triple_swapping.slot_mode"))
                .setSaveConsumer(newValue -> config.selectionMode = newValue)
                .build());

        // Subcategory / entries for Hotbar Slots (1-9)
        weaponCategory.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.start_slot"),
                        config.startSlot + 1, 1, 9)
                .setDefaultValue(1)
                .setTooltip(Text.translatable("tooltip.triple_swapping.start_slot"))
                .setSaveConsumer(val -> config.startSlot = val - 1)
                .build());

        weaponCategory.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.reach_slot"),
                        config.reachSlot + 1, 1, 9)
                .setDefaultValue(2)
                .setTooltip(Text.translatable("tooltip.triple_swapping.reach_slot"))
                .setSaveConsumer(val -> config.reachSlot = val - 1)
                .build());

        weaponCategory.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.attack_slot"),
                        config.attackSlot + 1, 1, 9)
                .setDefaultValue(3)
                .setTooltip(Text.translatable("tooltip.triple_swapping.attack_slot"))
                .setSaveConsumer(val -> config.attackSlot = val - 1)
                .build());

        // Dynamic Item Identifiers
        weaponCategory.addEntry(entryBuilder.startStrField(
                        Text.translatable("text.triple_swapping.start_item_id"),
                        config.startItemId)
                .setDefaultValue("minecraft:diamond_sword")
                .setTooltip(Text.translatable("tooltip.triple_swapping.start_item_id"))
                .setSaveConsumer(val -> config.startItemId = val)
                .build());

        weaponCategory.addEntry(entryBuilder.startStrField(
                        Text.translatable("text.triple_swapping.reach_item_id"),
                        config.reachItemId)
                .setDefaultValue("spear")
                .setTooltip(Text.translatable("tooltip.triple_swapping.reach_item_id"))
                .setSaveConsumer(val -> config.reachItemId = val)
                .build());

        weaponCategory.addEntry(entryBuilder.startStrField(
                        Text.translatable("text.triple_swapping.attack_item_id"),
                        config.attackItemId)
                .setDefaultValue("minecraft:mace")
                .setTooltip(Text.translatable("tooltip.triple_swapping.attack_item_id"))
                .setSaveConsumer(val -> config.attackItemId = val)
                .build());

        // ---------------------------------------------------------
        // Category 3: Visuals & HUD
        // ---------------------------------------------------------
        ConfigCategory visualCategory = builder.getOrCreateCategory(
                Text.translatable("category.triple_swapping.visuals")
        );

        visualCategory.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("text.triple_swapping.hud_indicator"),
                        config.hudIndicator)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.triple_swapping.hud_indicator"))
                .setSaveConsumer(val -> config.hudIndicator = val)
                .build());

        // Save callback
        builder.setSavingRunnable(config::save);

        return builder.build();
    }
}
