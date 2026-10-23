package net.tripleswapping.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.tripleswapping.TripleSwappingMod;
import net.tripleswapping.config.TripleSwappingConfig;

public class TripleSwapManager {
    private static final TripleSwapManager INSTANCE = new TripleSwapManager();

    private boolean isSwapping = false;
    private long lastSwapTimeMs = 0L;
    private int pendingReturnSlot = -1;
    private int returnTicksRemaining = -1;

    private TripleSwapManager() {
    }

    public static TripleSwapManager getInstance() {
        return INSTANCE;
    }

    public boolean isSwapping() {
        return isSwapping;
    }

    public void resetPendingState() {
        pendingReturnSlot = -1;
        returnTicksRemaining = -1;
        isSwapping = false;
    }

    /**
     * Advances post-attack automatic return to Step 1 (Start Slot) so start-of-tick weapon attributes
     * (e.g. Sword attack damage & speed) re-apply cleanly on the server before the next swing.
     */
    public void onClientTick(MinecraftClient client) {
        if (pendingReturnSlot < 0 || returnTicksRemaining < 0) {
            return;
        }
        if (client.player == null || client.getNetworkHandler() == null) {
            resetPendingState();
            return;
        }

        if (returnTicksRemaining == 0) {
            int slotToRestore = pendingReturnSlot;
            pendingReturnSlot = -1;
            returnTicksRemaining = -1;

            if (slotToRestore >= 0 && slotToRestore < 9
                    && client.player.getInventory().getSelectedSlot() != slotToRestore) {
                client.player.getInventory().setSelectedSlot(slotToRestore);
                client.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slotToRestore));
            }
        } else {
            returnTicksRemaining--;
        }
    }

    /**
     * Resolves hotbar slots (0-8) for the three stages: Start -> Reach -> Attack.
     * Returns null if any required item or slot cannot be resolved on the hotbar.
     */
    public int[] resolveSlots(ClientPlayerEntity player, TripleSwappingConfig config) {
        int start = resolveStartSlot(player, config);
        int reach = resolveReachSlot(player, config);
        int attack = resolveAttackSlot(player, config);

        if (start == -1 || reach == -1 || attack == -1) {
            return null;
        }
        return new int[]{start, reach, attack};
    }

    public int resolveStartSlot(ClientPlayerEntity player, TripleSwappingConfig config) {
        if (config.slotMode == TripleSwappingConfig.SlotMode.HOTBAR_SLOTS) {
            return clampSlot(config.startSlot - 1);
        }
        return findBestItemSlotInHotbar(player, config.startItemId, false);
    }

    public int resolveReachSlot(ClientPlayerEntity player, TripleSwappingConfig config) {
        if (config.slotMode == TripleSwappingConfig.SlotMode.HOTBAR_SLOTS) {
            return clampSlot(config.reachSlot - 1);
        }
        return findBestItemSlotInHotbar(player, config.reachItemId, true);
    }

    public int resolveAttackSlot(ClientPlayerEntity player, TripleSwappingConfig config) {
        if (config.slotMode == TripleSwappingConfig.SlotMode.HOTBAR_SLOTS) {
            return clampSlot(config.attackSlot - 1);
        }
        return findBestItemSlotInHotbar(player, config.attackItemId, false);
    }

    private int clampSlot(int slot) {
        return Math.max(0, Math.min(8, slot));
    }

    /**
     * Searches hotbar slots (0-8) and selects the highest-tier matching item.
     * Supports full registry identifiers ("minecraft:netherite_spear"), keywords ("spear", "sword", "mace"),
     * and 1.21.11 {@code minecraft:attack_range} / {@code minecraft:piercing_weapon} data components.
     */
    private int findBestItemSlotInHotbar(ClientPlayerEntity player, String query, boolean preferExtendedReachComponent) {
        if (query == null || query.trim().isEmpty()) {
            return -1;
        }
        String normalized = query.trim().toLowerCase();

        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) {
                continue;
            }

            Identifier itemId = Registries.ITEM.getId(stack.getItem());
            String fullId = itemId.toString().toLowerCase();
            String path = itemId.getPath().toLowerCase();

            boolean matchesId = fullId.equals(normalized)
                    || path.equals(normalized)
                    || path.contains(normalized);

            boolean matchesSpearComponent = preferExtendedReachComponent
                    && "spear".equals(normalized)
                    && (stack.contains(DataComponentTypes.ATTACK_RANGE)
                        || stack.contains(DataComponentTypes.PIERCING_WEAPON));

            if (!matchesId && !matchesSpearComponent) {
                continue;
            }

            int score = scoreMaterialTier(path);
            if (fullId.equals(normalized) || path.equals(normalized)) {
                score += 100;
            }
            if (preferExtendedReachComponent && stack.contains(DataComponentTypes.ATTACK_RANGE)) {
                score += 50;
            }

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private int scoreMaterialTier(String itemPath) {
        if (itemPath.startsWith("netherite_")) return 7;
        if (itemPath.startsWith("diamond_")) return 6;
        if (itemPath.startsWith("iron_")) return 5;
        if (itemPath.startsWith("copper_")) return 4;
        if (itemPath.startsWith("stone_")) return 3;
        if (itemPath.startsWith("golden_")) return 2;
        if (itemPath.startsWith("wooden_")) return 1;
        return 4;
    }

    /**
     * Executes the authentic Minecraft 1.21.11 Triple Swap combat sequence within a single tick window:
     * <ol>
     *   <li>Start-of-tick attributes cached on server from {@code startSlot} (e.g. Sword damage & attack speed).</li>
     *   <li>{@code UpdateSelectedSlotC2SPacket(reachSlot)} transitions through the 1.21.11 Spear ({@code AttackRangeComponent}).</li>
     *   <li>{@code UpdateSelectedSlotC2SPacket(attackSlot)} selects the finishing weapon (e.g. Mace) prior to attack.</li>
     *   <li>{@code PlayerInteractEntityC2SPacket.attack(target, sneaking)} + {@code swingHand(MAIN_HAND)} triggers {@code player.attack(target)}
     *       before {@code LivingEntity#sendEquipmentChanges} strips the start weapon's attributes.</li>
     *   <li>Optionally schedules return to {@code startSlot} so the next tick re-arms Start Item attributes automatically.</li>
     * </ol>
     */
    public boolean executeTripleSwapAttack(MinecraftClient client, Entity target) {
        if (isSwapping || client.player == null || client.getNetworkHandler() == null) {
            return false;
        }

        TripleSwappingConfig config = TripleSwappingConfig.getInstance();
        if (!config.enabled) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (now - lastSwapTimeMs < config.clientSwapDelayMs) {
            return false;
        }

        ClientPlayerEntity player = client.player;
        ClientPlayNetworkHandler netHandler = client.getNetworkHandler();

        int[] slots = resolveSlots(player, config);
        if (slots == null) {
            return false;
        }

        int startSlot = slots[0];
        int reachSlot = slots[1];
        int attackSlot = slots[2];

        isSwapping = true;
        lastSwapTimeMs = now;
        pendingReturnSlot = -1;
        returnTicksRemaining = -1;

        try {
            int currentSlot = player.getInventory().getSelectedSlot();

            // Phase 1: Ensure start slot packet consistency if not already holding Start Slot
            if (currentSlot != startSlot && startSlot != reachSlot) {
                player.getInventory().setSelectedSlot(startSlot);
                netHandler.sendPacket(new UpdateSelectedSlotC2SPacket(startSlot));
            }

            // Phase 2: Transition through Reach Weapon (1.21.11 Spear — 4.5m AttackRangeComponent)
            if (player.getInventory().getSelectedSlot() != reachSlot) {
                player.getInventory().setSelectedSlot(reachSlot);
                netHandler.sendPacket(new UpdateSelectedSlotC2SPacket(reachSlot));
            }

            // Phase 3: Transition to Finishing Weapon (e.g. Mace) before Interact packet
            if (player.getInventory().getSelectedSlot() != attackSlot) {
                player.getInventory().setSelectedSlot(attackSlot);
                netHandler.sendPacket(new UpdateSelectedSlotC2SPacket(attackSlot));
            }

            // Phase 4: Dispatch melee attack & main-hand swing within the same tick window
            netHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
            player.resetLastAttackedTicks();
            player.swingHand(Hand.MAIN_HAND);

            // Phase 5: Return to Start Slot (same-tick if delay == 0, or on subsequent client tick)
            if (config.autoSwapBack && startSlot != attackSlot) {
                if (config.swapBackDelayTicks <= 0) {
                    player.getInventory().setSelectedSlot(startSlot);
                    netHandler.sendPacket(new UpdateSelectedSlotC2SPacket(startSlot));
                } else {
                    pendingReturnSlot = startSlot;
                    returnTicksRemaining = config.swapBackDelayTicks;
                }
            }

            return true;
        } catch (Exception e) {
            TripleSwappingMod.LOGGER.error("Error executing 1.21.11 triple swap sequence", e);
            return false;
        } finally {
            isSwapping = false;
        }
    }

    /**
     * Triggered from dedicated keybind or hold-key mode.
     * Raycasts out to the active 1.21.11 Spear extended reach and executes a triple swap if a valid target is found.
     */
    public void attemptKeybindSwap(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        TripleSwappingConfig config = TripleSwappingConfig.getInstance();
        double effectiveReach = RaycastHelper.getEffectiveReachDistance(client.player, config);
        RaycastHelper.TargetResult result = RaycastHelper.findTargetInExtendedReach(client, effectiveReach);
        if (result != null && RaycastHelper.isAttackableTarget(result.entity())) {
            executeTripleSwapAttack(client, result.entity());
        }
    }

    /**
     * Intercepts normal client attack attempts (e.g. left-click in {@code MinecraftClient#doAttack}).
     * Also intercepts 1.21.11 Spear left-clicks before vanilla converts them into {@code STAB} packets when triple-swapping.
     */
    public boolean handleClientAttackAttempt(MinecraftClient client) {
        TripleSwappingConfig config = TripleSwappingConfig.getInstance();
        if (!config.enabled || isSwapping) {
            return false;
        }

        if (config.triggerMode == TripleSwappingConfig.TriggerMode.KEYBIND) {
            return false;
        }

        double effectiveReach = RaycastHelper.getEffectiveReachDistance(client.player, config);
        RaycastHelper.TargetResult result = RaycastHelper.findTargetInExtendedReach(client, effectiveReach);
        if (result == null || !RaycastHelper.isAttackableTarget(result.entity())) {
            return false;
        }

        if (config.triggerMode == TripleSwappingConfig.TriggerMode.OUT_OF_NORMAL_RANGE) {
            if (result.distance() <= config.normalReach) {
                return false;
            }
        }

        return executeTripleSwapAttack(client, result.entity());
    }
}
