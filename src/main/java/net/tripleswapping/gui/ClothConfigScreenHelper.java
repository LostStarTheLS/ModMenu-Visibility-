package net.tripleswapping.gui;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.tripleswapping.config.TripleSwappingConfig;

public class ClothConfigScreenHelper {

    public static Screen createClothConfigScreen(Screen parent) {
        TripleSwappingConfig config = TripleSwappingConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("text.triple_swapping.title"));

        builder.setSavingRunnable(TripleSwappingConfig::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 1. General & Combat Category
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.triple_swapping.general"));

        general.addEntry(entryBuilder.startBooleanToggle(Text.translatable("text.triple_swapping.enabled"), config.enabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.triple_swapping.enabled"))
                .setSaveConsumer(newValue -> config.enabled = newValue)
                .build());

        general.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("text.triple_swapping.trigger_mode"),
                        TripleSwappingConfig.TriggerMode.class,
                        config.triggerMode)
                .setDefaultValue(TripleSwappingConfig.TriggerMode.OUT_OF_NORMAL_RANGE)
                .setTooltip(Text.translatable("tooltip.triple_swapping.trigger_mode"))
                .setEnumNameProvider(enumValue -> {
                    TripleSwappingConfig.TriggerMode mode = (TripleSwappingConfig.TriggerMode) enumValue;
                    return switch (mode) {
                        case ON_ATTACK -> Text.translatable("text.triple_swapping.trigger_mode.on_attack");
                        case KEYBIND -> Text.translatable("text.triple_swapping.trigger_mode.keybind");
                        case HOLD_KEY -> Text.translatable("text.triple_swapping.trigger_mode.hold_key");
                        case OUT_OF_NORMAL_RANGE -> Text.translatable("text.triple_swapping.trigger_mode.out_of_range");
                    };
                })
                .setSaveConsumer(newValue -> config.triggerMode = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("text.triple_swapping.use_attack_range_component"),
                        config.useItemAttackRangeComponent)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.triple_swapping.use_attack_range_component"))
                .setSaveConsumer(newValue -> config.useItemAttackRangeComponent = newValue)
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("text.triple_swapping.reach_distance"),
                        config.reachDistance)
                .setDefaultValue(4.5)
                .setMin(3.0)
                .setMax(6.5)
                .setTooltip(Text.translatable("tooltip.triple_swapping.reach_distance"))
                .setSaveConsumer(newValue -> config.reachDistance = newValue)
                .build());

        general.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("text.triple_swapping.hitbox_margin"),
                        config.hitboxMargin)
                .setDefaultValue(0.125)
                .setMin(0.0)
                .setMax(0.5)
                .setTooltip(Text.translatable("tooltip.triple_swapping.hitbox_margin"))
                .setSaveConsumer(newValue -> config.hitboxMargin = newValue)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("text.triple_swapping.auto_swap_back"),
                        config.autoSwapBack)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.triple_swapping.auto_swap_back"))
                .setSaveConsumer(newValue -> config.autoSwapBack = newValue)
                .build());

        general.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.swap_back_delay"),
                        config.swapBackDelayTicks,
                        0,
                        5)
                .setDefaultValue(1)
                .setTooltip(Text.translatable("tooltip.triple_swapping.swap_back_delay"))
                .setSaveConsumer(newValue -> config.swapBackDelayTicks = newValue)
                .build());

        general.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.client_delay"),
                        config.clientSwapDelayMs,
                        0,
                        250)
                .setDefaultValue(50)
                .setTooltip(Text.translatable("tooltip.triple_swapping.client_delay"))
                .setSaveConsumer(newValue -> config.clientSwapDelayMs = newValue)
                .build());

        // 2. Weapon & Slot Configuration Category
        ConfigCategory weapons = builder.getOrCreateCategory(Text.translatable("category.triple_swapping.weapons"));

        weapons.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("text.triple_swapping.slot_mode"),
                        TripleSwappingConfig.SlotMode.class,
                        config.slotMode)
                .setDefaultValue(TripleSwappingConfig.SlotMode.HOTBAR_SLOTS)
                .setTooltip(Text.translatable("tooltip.triple_swapping.slot_mode"))
                .setEnumNameProvider(enumValue -> {
                    TripleSwappingConfig.SlotMode mode = (TripleSwappingConfig.SlotMode) enumValue;
                    return switch (mode) {
                        case HOTBAR_SLOTS -> Text.translatable("text.triple_swapping.slot_mode.hotbar_slots");
                        case ITEM_IDS -> Text.translatable("text.triple_swapping.slot_mode.item_ids");
                    };
                })
                .setSaveConsumer(newValue -> config.slotMode = newValue)
                .build());

        weapons.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.start_slot"),
                        config.startSlot,
                        1,
                        9)
                .setDefaultValue(1)
                .setTooltip(Text.translatable("tooltip.triple_swapping.start_slot"))
                .setSaveConsumer(newValue -> config.startSlot = newValue)
                .build());

        weapons.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.reach_slot"),
                        config.reachSlot,
                        1,
                        9)
                .setDefaultValue(2)
                .setTooltip(Text.translatable("tooltip.triple_swapping.reach_slot"))
                .setSaveConsumer(newValue -> config.reachSlot = newValue)
                .build());

        weapons.addEntry(entryBuilder.startIntSlider(
                        Text.translatable("text.triple_swapping.attack_slot"),
                        config.attackSlot,
                        1,
                        9)
                .setDefaultValue(3)
                .setTooltip(Text.translatable("tooltip.triple_swapping.attack_slot"))
                .setSaveConsumer(newValue -> config.attackSlot = newValue)
                .build());

        weapons.addEntry(entryBuilder.startStrField(
                        Text.translatable("text.triple_swapping.start_item_id"),
                        config.startItemId)
                .setDefaultValue("sword")
                .setTooltip(Text.translatable("tooltip.triple_swapping.start_item_id"))
                .setSaveConsumer(newValue -> config.startItemId = newValue)
                .build());

        weapons.addEntry(entryBuilder.startStrField(
                        Text.translatable("text.triple_swapping.reach_item_id"),
                        config.reachItemId)
                .setDefaultValue("spear")
                .setTooltip(Text.translatable("tooltip.triple_swapping.reach_item_id"))
                .setSaveConsumer(newValue -> config.reachItemId = newValue)
                .build());

        weapons.addEntry(entryBuilder.startStrField(
                        Text.translatable("text.triple_swapping.attack_item_id"),
                        config.attackItemId)
                .setDefaultValue("minecraft:mace")
                .setTooltip(Text.translatable("tooltip.triple_swapping.attack_item_id"))
                .setSaveConsumer(newValue -> config.attackItemId = newValue)
                .build());

        // 3. Visuals & HUD Category
        ConfigCategory visuals = builder.getOrCreateCategory(Text.translatable("category.triple_swapping.visuals"));

        visuals.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("text.triple_swapping.hud_indicator"),
                        config.showHudIndicator)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("tooltip.triple_swapping.hud_indicator"))
                .setSaveConsumer(newValue -> config.showHudIndicator = newValue)
                .build());

        return builder.build();
    }
}
