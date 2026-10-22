# Triple Swapping (Minecraft 1.21.1 Fabric Mod)

A client-side Fabric mod template implementing the **Triple Swapping** combat technique for Minecraft 1.21.x (1.21.1 / 1.21.11).

Triple swapping exploits client-side tick timing, weapon reach calculations, and server-side attribute update cycles to combine the benefits of three items into a single attack:
1. **Initial Item (Attributes):** e.g., Sword (provides start-of-tick attack speed and baseline attributes).
2. **Intermediate Item (Reach / Range):** e.g., Spear / Trident (provides extended entity reach of ~4.5 blocks, up 50% from the normal 3.0 blocks, and an expanded targeted entity hitbox).
3. **Finishing Item (Impact / Enchantments):** e.g., Mace (applies heavy smash damage, enchantments, and impact properties).

---

## Modern Public GUI Library & "Zip Inside" (Jar-in-Jar)

To make the configuration interface look clean, modern, and native to Minecraft, the project integrates **Cloth Config** and bundles it using Fabric's **Jar-in-Jar (JiJ / "zip inside")** packaging:

### 1. Cloth Config Integration
- **Category Tabs:** Organized into *General & Combat*, *Weapon & Slot Configuration*, and *Visuals & HUD*.
- **Interactive Controls:** Smooth sliders for reach distance (3.0 – 6.0 blocks) and hotbar slots (1–9), dropdown enum selectors, and item ID text fields.
- **Rich Hover Tooltips:** Explanations and advice on each setting directly in-game.
- **Mod Menu Integration:** Accessible directly via Mod Menu's mod settings button or via in-game keybind (`H`).

### 2. Jar-in-Jar (`include` / "Zip Inside")
In `build.gradle`, Cloth Config is packaged using Loom's `include` directive:
```gradle
modApi("me.shedaniel.cloth:cloth-config-fabric:${project.cloth_config_version}") {
    exclude(group: "net.fabricmc.fabric-api")
}
// Bundles the library zip/jar inside META-INF/jars of the built mod JAR
include("me.shedaniel.cloth:cloth-config-fabric:${project.cloth_config_version}")
```
This means the compiled mod is completely self-contained—users do not need to download separate external library files.

### 3. Graceful Fallback Architecture
The mod uses `TripleSwappingGuiFactory`:
- When Cloth Config is present (via JiJ or installed mods), it serves the rich Cloth Config screen.
- If Cloth Config is absent, it gracefully falls back to the customized built-in `TripleSwappingConfigScreen` without crashing.

---

## How It Works (The Mechanics)

### 1. Attribute Recalculation Timing
* At the beginning of each game tick, the server recalculates entity attributes via equipment update checks (`detectEquipmentUpdates`).
* When an attack is processed, the server computes attributes based on what was held at the start of that tick, but evaluates weapon enchantments and properties based on the currently selected item packet received with the attack.

### 2. The 50ms (1-Tick) Execution Window
1. **Tick Start:** Player has Item 1 (Sword) selected. The server records Sword attributes.
2. **Pre-Raycast Transition:** The client switches active hotbar focus to Item 2 (Spear) client-side before sending the network update packet.
3. **Target Calculation:** The client performs targeted entity raycasting using the spear's extended reach (up to 4.5 blocks).
4. **Packet Dispatch & Attack:** The client switches to Item 3 (Mace), dispatches `UpdateSelectedSlotC2SPacket(attackSlot)` and immediately sends `PlayerInteractEntityC2SPacket.attack(target)`.
5. **Server Resolution:** The server evaluates the attack: it sees the player holding the Mace, attacks the target acquired via Spear range, and retains the Sword's attack attributes.

---

## Features

- **Select When to Do It:**
  - **Only When Target > 3 Blocks:** Seamless mode—performs standard attacks if the target is within 3.0 blocks, but automatically executes the triple swap if the target is between 3.0 and 4.5 blocks away.
  - **On Attack (Auto):** Performs the triple swap on every attack when items are present.
  - **Dedicated Keybind:** Trigger the triple swap on demand using a dedicated key (default: `R`).
  - **Hold Trigger Key:** Normal attack performs triple swap only while holding the trigger key.
  - **Quick Toggle:** Instant on/off toggle via keybind (default: `V`).

- **Select What Items to Do It On:**
  - **Fixed Hotbar Slots:** Explicitly choose slots 1–9 for Step 1 (Attributes), Step 2 (Reach), and Step 3 (Attack).
  - **Auto-Detect by Item ID:** Scans your hotbar for items matching queries (e.g., `sword`, `spear`/`trident`, `mace`).

- **In-Game GUI & Mod Menu Support:**
  - Modern Cloth Config screen with category tabs, sliders, and tooltips.
  - Opened via `H` or through Mod Menu.
  - Adjust reach distance (3.5 to 5.0 blocks).
  - Toggle HUD overlay showing current state and slot chain.

---

## Controls (Default Keybindings)

| Key | Action |
|-----|--------|
| `V` | Toggle Triple Swapping on / off |
| `R` | Trigger Triple Swap attack / hold modifier |
| `H` | Open Triple Swapping In-Game Configuration GUI |

---

## Configuration File

The mod automatically saves settings to `.minecraft/config/triple_swapping.json`:

```json
{
  "enabled": true,
  "triggerMode": "OUT_OF_RANGE_ONLY",
  "selectionMode": "BY_HOTBAR_SLOT",
  "startSlot": 0,
  "reachSlot": 1,
  "attackSlot": 2,
  "startItemId": "minecraft:diamond_sword",
  "reachItemId": "spear",
  "attackItemId": "minecraft:mace",
  "reachDistance": 4.5,
  "normalReach": 3.0,
  "clientDelayMs": 50,
  "hudIndicator": true
}
```

---

## Building and Developing

### Prerequisites
- Java 21 JDK
- Internet access to download Gradle dependencies and Fabric Loom mappings

### Building the Mod JAR
Run Gradle wrapper from the repository root:

```bash
# Linux / macOS
./gradlew build

# Windows
gradlew.bat build
```

The compiled mod JAR with embedded Cloth Config will be placed in `build/libs/`.

---

## License

This project is licensed under the MIT License.
