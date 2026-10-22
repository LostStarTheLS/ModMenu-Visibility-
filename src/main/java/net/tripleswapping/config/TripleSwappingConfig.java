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
    private static final File CONFIG_FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "triple_swapping.json"
    );

    private static TripleSwappingConfig instance;

    public enum TriggerMode {
        ON_ATTACK("On Attack (Auto)"),
        KEYBIND("Dedicated Keybind"),
        HOLD_KEY("Hold Trigger Key"),
        OUT_OF_RANGE_ONLY("Only When Target > 3 Blocks");

        private final String displayName;

        TriggerMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public TriggerMode next() {
            TriggerMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    public enum SelectionMode {
        BY_HOTBAR_SLOT("Fixed Hotbar Slot"),
        BY_ITEM_ID("Auto-Detect Item in Hotbar");

        private final String displayName;

        SelectionMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public SelectionMode next() {
            SelectionMode[] values = values();
            return values[(this.ordinal() + 1) % values.length];
        }
    }

    // Toggle
    public boolean enabled = true;

    // Trigger Mode
    public TriggerMode triggerMode = TriggerMode.OUT_OF_RANGE_ONLY;

    // Selection Mode
    public SelectionMode selectionMode = SelectionMode.BY_HOTBAR_SLOT;

    // Fixed Hotbar Slots (0-8 index)
    // Step 1: Start item (Sword - provides start-of-tick attributes like attack speed & base damage)
    public int startSlot = 0; // Slot 1
    // Step 2: Reach item (Spear/Trident - provides extended targeted entity calculation / range)
    public int reachSlot = 1; // Slot 2
    // Step 3: Attack item (Mace - provides impact properties, enchantments & smash attack)
    public int attackSlot = 2; // Slot 3

    // Dynamic Item Identifiers for BY_ITEM_ID mode
    public String startItemId = "minecraft:diamond_sword";
    public String reachItemId = "spear"; // Matches spear or trident
    public String attackItemId = "minecraft:mace";

    // Reach parameters
    public double reachDistance = 4.5; // Default Minecraft reach is 3.0 blocks; spear extends to ~4.5 blocks (+50%)
    public double normalReach = 3.0;

    // Timing parameters
    public int clientDelayMs = 50; // Window of time for slot transition (1 tick = 50ms)

    // HUD Indicator
    public boolean hudIndicator = true;

    public static TripleSwappingConfig get() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    public static TripleSwappingConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                TripleSwappingConfig loaded = GSON.fromJson(reader, TripleSwappingConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException e) {
                TripleSwappingMod.LOGGER.error("Failed to load Triple Swapping config", e);
            }
        }
        TripleSwappingConfig created = new TripleSwappingConfig();
        created.save();
        return created;
    }

    public void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            TripleSwappingMod.LOGGER.error("Failed to save Triple Swapping config", e);
        }
    }
}
