package net.tripleswapping.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.tripleswapping.TripleSwappingMod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TripleSwappingConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("triple_swapping.json")
            .toFile();

    private static TripleSwappingConfig INSTANCE;

    public enum TriggerMode {
        ON_ATTACK,
        KEYBIND,
        HOLD_KEY,
        OUT_OF_NORMAL_RANGE
    }

    public enum SlotMode {
        HOTBAR_SLOTS,
        ITEM_IDS
    }

    // Master toggle
    public boolean enabled = true;

    // Triggering activation mode
    public TriggerMode triggerMode = TriggerMode.OUT_OF_NORMAL_RANGE;

    // Weapon selection method
    public SlotMode slotMode = SlotMode.HOTBAR_SLOTS;

    // Fixed Hotbar Slots (1-9 for human display, mapped internally to 0-8)
    public int startSlot = 1;   // Step 1: Start item (e.g., Netherite/Diamond Sword — start-of-tick attributes)
    public int reachSlot = 2;   // Step 2: Reach item (1.21.11 Spear — 4.5m reach + 0.125 hitbox margin)
    public int attackSlot = 3;  // Step 3: Finishing item (e.g., Mace — Smash/Breach/Density/Wind Burst impact)

    // Auto-detect item identifiers/keywords for Minecraft 1.21.11
    public String startItemId = "sword";
    public String reachItemId = "spear";
    public String attackItemId = "minecraft:mace";

    // Reach & 1.21.11 AttackRangeComponent configuration
    public boolean useItemAttackRangeComponent = true;
    public double reachDistance = 4.5;
    public double normalReach = 3.0;
    public double spearMinRange = 2.0;
    public double hitboxMargin = 0.125;

    // Post-attack automatic return to Start Slot so consecutive hits retain Sword attributes
    public boolean autoSwapBack = true;
    public int swapBackDelayTicks = 1;

    // Minimum transition window cooldown between automated triple swaps (ms)
    public int clientSwapDelayMs = 50;

    // Visual & HUD configuration
    public boolean showHudIndicator = true;

    public static TripleSwappingConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, TripleSwappingConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new TripleSwappingConfig();
                } else {
                    INSTANCE.sanitize();
                }
            } catch (IOException | RuntimeException e) {
                TripleSwappingMod.LOGGER.error("Failed to load config file, resetting to defaults.", e);
                INSTANCE = new TripleSwappingConfig();
            }
        } else {
            INSTANCE = new TripleSwappingConfig();
            save();
        }
    }

    public void sanitize() {
        if (triggerMode == null) triggerMode = TriggerMode.OUT_OF_NORMAL_RANGE;
        if (slotMode == null) slotMode = SlotMode.HOTBAR_SLOTS;
        startSlot = clamp(startSlot, 1, 9);
        reachSlot = clamp(reachSlot, 1, 9);
        attackSlot = clamp(attackSlot, 1, 9);
        if (startItemId == null || startItemId.isBlank()) startItemId = "sword";
        if (reachItemId == null || reachItemId.isBlank()) reachItemId = "spear";
        if (attackItemId == null || attackItemId.isBlank()) attackItemId = "minecraft:mace";
        if ("trident".equalsIgnoreCase(reachItemId.trim())) {
            // Seamlessly upgrade legacy 1.21.1 default keyword to 1.21.11 Spear
            reachItemId = "spear";
        }
        reachDistance = Math.max(3.0, Math.min(6.5, reachDistance));
        normalReach = Math.max(1.0, Math.min(6.0, normalReach));
        hitboxMargin = Math.max(0.0, Math.min(0.5, hitboxMargin));
        swapBackDelayTicks = clamp(swapBackDelayTicks, 0, 5);
        clientSwapDelayMs = clamp(clientSwapDelayMs, 0, 500);
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static void save() {
        if (INSTANCE == null) {
            INSTANCE = new TripleSwappingConfig();
        }
        INSTANCE.sanitize();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            TripleSwappingMod.LOGGER.error("Failed to save config file.", e);
        }
    }

    public void resetToDefaults() {
        TripleSwappingConfig defaults = new TripleSwappingConfig();
        this.enabled = defaults.enabled;
        this.triggerMode = defaults.triggerMode;
        this.slotMode = defaults.slotMode;
        this.startSlot = defaults.startSlot;
        this.reachSlot = defaults.reachSlot;
        this.attackSlot = defaults.attackSlot;
        this.startItemId = defaults.startItemId;
        this.reachItemId = defaults.reachItemId;
        this.attackItemId = defaults.attackItemId;
        this.useItemAttackRangeComponent = defaults.useItemAttackRangeComponent;
        this.reachDistance = defaults.reachDistance;
        this.normalReach = defaults.normalReach;
        this.spearMinRange = defaults.spearMinRange;
        this.hitboxMargin = defaults.hitboxMargin;
        this.autoSwapBack = defaults.autoSwapBack;
        this.swapBackDelayTicks = defaults.swapBackDelayTicks;
        this.clientSwapDelayMs = defaults.clientSwapDelayMs;
        this.showHudIndicator = defaults.showHudIndicator;
        save();
    }
}
