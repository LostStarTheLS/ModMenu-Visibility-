# Triple Swapping — Minecraft 1.21.11 Fabric Mod

A client-side Fabric mod for **Minecraft 1.21.11 ("Mounts of Mayhem")** that automates and visualizes the **Triple Swapping** combat technique: combining start-of-tick weapon attributes (e.g., **Sword** attack damage & attack speed), intermediate **1.21.11 Spear** extended reach (`minecraft:attack_range` — **4.5 blocks** + **0.125 hitbox margin**), and finishing weapon impact & enchantments (e.g., **Mace** Smash / Breach / Density / Wind Burst).

---

## How Triple Swapping Works in Minecraft 1.21.11

Minecraft **1.21.11** introduced vanilla **Spears** (`netherite_spear`, `diamond_spear`, `iron_spear`, `copper_spear`, `stone_spear`, `golden_spear`, `wooden_spear`) powered by the `minecraft:attack_range` (`AttackRangeComponent`) data component:
- **Survival Spear Reach**: `min_range = 2.0`, `max_range = 4.5` blocks (`6.5` in Creative), `hitbox_margin = 0.125` blocks.
- **Server Equipment Update Timing**: On the server, `PlayerInteractEntityC2SPacket` (`attack`) runs synchronously when received during packet handling—**before** `LivingEntity#sendEquipmentChanges()` recalculates main-hand attribute modifiers at the end of the entity tick.

When a **Triple Swap** executes within a single tick window:
1. **Step 1 — Start Item (e.g., Netherite Sword)**: Held at the end of the prior tick so the server caches its `attack_damage` and fast `attack_speed` attributes.
2. **Step 2 — Reach Item (1.21.11 Spear)**: Raycasts with `AttackRangeComponent` (`4.5m` reach + `0.125` expanded hitbox margin, ignoring the `0–2.0m` close-range jab restriction when combining weapons) and sends `UpdateSelectedSlotC2SPacket(reachSlot)`.
3. **Step 3 — Finishing Item (e.g., Mace / Axe)**: Sends `UpdateSelectedSlotC2SPacket(attackSlot)` immediately followed by `PlayerInteractEntityC2SPacket.attack(target, sneaking)` and `HandSwingC2SPacket`, applying Mace fall bonus damage + Breach / Density / Wind Burst (or Axe shield disable) on top of the Sword's cached base damage and cooldown!
4. **Step 4 — Automatic Return to Start Slot**: Automatically returns selection to `startSlot` so your next swing starts with full Sword attributes armed again.

---

## Features

- **Native 1.21.11 Spear & `AttackRangeComponent` Support**:
  - Automatically reads `DataComponentTypes.ATTACK_RANGE` (`getEffectiveMaxRange(player)` and `hitboxMargin()`) directly from held/hotbar 1.21.11 Spears (`netherite_spear` through `wooden_spear`, plus custom data-pack weapons).
  - Prioritizes highest material tier (`netherite` › `diamond` › `iron` › `copper` › `stone` › `golden` › `wooden`) when auto-detecting weapons by keyword.
- **4 Trigger Activation Modes**:
  - **Only When Target > 3 Blocks (`>3m AUTO`)**: Seamless mode—attacks normally inside `3.0m` and automatically triple-swaps on targets between `3.0m` and `4.5m`.
  - **On Attack (`ALWAYS`)**: Executes the full triple-swap chain on every melee attack (including inside `0–3.0m`).
  - **On Dedicated Keybind (`KEYBIND`)**: Executes on pressing the dedicated trigger hotkey (default `R`).
  - **While Holding Key (`HOLD`)**: Automatically triggers whenever cooldown reaches `≥ 90%` while holding `R`.
- **Bundled Cloth Config (`21.11.160`) + Mod Menu (`17.0.1`) Integration**:
  - Embeds Cloth Config directly via Jar-in-Jar (`META-INF/jars`)—no separate Cloth Config download required.
  - Full **Mod Menu** config screen integration plus built-in standalone `Screen` fallback.
- **Modern 1.21.11 HUD Overlay (`HudElementRegistry`)**:
  - Live weapon icon chain (`Start › Spear › Finish`) with active slot highlight, resolved reach readout, and real-time target distance & health bar card.

---

## Default Controls

| Action | Default Key | Category |
| :--- | :---: | :--- |
| **Toggle Triple Swapping** | `V` | `Triple Swapping` |
| **Trigger Triple Swap Attack** | `R` | `Triple Swapping` |
| **Open Triple Swapping Config** | `O` | `Triple Swapping` |

---

## 1.21.11 Build Matrix

| Dependency | Version |
| :--- | :--- |
| **Minecraft** | `1.21.11` |
| **Yarn Mappings** | `1.21.11+build.6` |
| **Fabric Loader** | `0.19.5` (compatible with `>=0.18.0`) |
| **Fabric API** | `0.141.6+1.21.11` |
| **Cloth Config (Bundled JiJ)** | `21.11.160` |
| **Mod Menu** | `17.0.1` |
| **Fabric Loom (`fabric-loom-remap`)** | `1.19.3` |
| **Gradle / Java** | Gradle `9.7.1` / Java `21` |

### Building from Source

```bash
./gradlew build
```

The remapped release JAR (with Cloth Config bundled inside) is output to:

```text
build/libs/triple-swapping-1.1.0.jar
```
