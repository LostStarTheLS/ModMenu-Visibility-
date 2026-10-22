package net.tripleswapping.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.tripleswapping.TripleSwappingMod;
import net.tripleswapping.config.TripleSwappingConfig;

public class TripleSwapManager {
    private static final TripleSwapManager INSTANCE = new TripleSwapManager();

    private boolean isExecuting = false;
    private long lastSwapTime = 0;

    public static TripleSwapManager getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves the hotbar slot index (0-8) for a given stage (Start, Reach, Attack).
     */
    public int resolveSlot(MinecraftClient client, int step) {
        TripleSwappingConfig config = TripleSwappingConfig.get();
        if (config.selectionMode == TripleSwappingConfig.SelectionMode.BY_HOTBAR_SLOT) {
            return switch (step) {
                case 1 -> Math.clamp(config.startSlot, 0, 8);
                case 2 -> Math.clamp(config.reachSlot, 0, 8);
                case 3 -> Math.clamp(config.attackSlot, 0, 8);
                default -> 0;
            };
        } else {
            // Match by Item ID
            String targetId = switch (step) {
                case 1 -> config.startItemId.toLowerCase();
                case 2 -> config.reachItemId.toLowerCase();
                case 3 -> config.attackItemId.toLowerCase();
                default -> "";
            };
            return findHotbarSlotByItemId(client, targetId, step);
        }
    }

    private int findHotbarSlotByItemId(MinecraftClient client, String targetQuery, int fallbackStep) {
        if (client.player == null) return fallbackStep - 1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                String id = Registries.ITEM.getId(stack.getItem()).toString().toLowerCase();
                if (id.contains(targetQuery)) {
                    return i;
                }
            }
        }
        // Fallback to configured numerical slot if matching item is not found
        TripleSwappingConfig config = TripleSwappingConfig.get();
        return switch (fallbackStep) {
            case 1 -> config.startSlot;
            case 2 -> config.reachSlot;
            case 3 -> config.attackSlot;
            default -> 0;
        };
    }

    /**
     * Attempts to perform the triple swap attack if conditions are met.
     */
    public boolean tryExecuteTripleSwap(MinecraftClient client, boolean triggeredByAttack) {
        TripleSwappingConfig config = TripleSwappingConfig.get();
        if (!config.enabled || isExecuting || client.player == null || client.world == null) {
            return false;
        }

        ClientPlayerEntity player = client.player;
        ClientPlayNetworkHandler network = client.getNetworkHandler();
        if (network == null) {
            return false;
        }

        // Raycast using the extended reach (spear range: up to reachDistance blocks)
        EntityHitResult hitResult = RaycastHelper.raycastEntity(client, config.reachDistance);
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return false;
        }

        Entity target = hitResult.getEntity();
        double distance = player.getCameraPosVec(1.0f).distanceTo(hitResult.getPos());

        // Check if out-of-range constraint is active
        if (config.triggerMode == TripleSwappingConfig.TriggerMode.OUT_OF_RANGE_ONLY) {
            if (distance <= config.normalReach) {
                // Within normal reach; normal attack applies
                return false;
            }
        }

        int startSlot = resolveSlot(client, 1);
        int reachSlot = resolveSlot(client, 2);
        int attackSlot = resolveSlot(client, 3);

        executeSequence(client, target, startSlot, reachSlot, attackSlot);
        return true;
    }

    /**
     * Executes the precise packet and slot switching sequence described by the triple swapping glitch:
     * 1. Start slot is held at tick start (server calculates weapon attributes like attack speed/modifiers).
     * 2. Reach slot is switched to for client-side target calculation (extended reach / expanded hitbox).
     * 3. Hotbar packet for attack slot is dispatched alongside attack packet.
     * 4. Server evaluates attack with attack slot weapon (mace) using start attributes and reach target.
     */
    public void executeSequence(MinecraftClient client, Entity target, int startSlot, int reachSlot, int attackSlot) {
        if (client.player == null || client.getNetworkHandler() == null) return;

        isExecuting = true;
        try {
            ClientPlayerEntity player = client.player;
            ClientPlayNetworkHandler netHandler = client.getNetworkHandler();

            // Ensure server has the start item (Sword) registered for attribute calculations
            if (player.getInventory().selectedSlot != startSlot) {
                player.getInventory().selectedSlot = startSlot;
                netHandler.sendPacket(new UpdateSelectedSlotC2SPacket(startSlot));
            }

            // Phase 2: Client internal transition to reach weapon (Spear)
            // Used for client-side reach detection and targeted entity evaluation
            player.getInventory().selectedSlot = reachSlot;

            // Phase 3: Transition to Attack Weapon (Mace) and send attack packet
            player.getInventory().selectedSlot = attackSlot;
            netHandler.sendPacket(new UpdateSelectedSlotC2SPacket(attackSlot));

            // Send Attack Packet
            netHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
            player.resetLastAttackedTicks();

            // Visual swing
            player.swingHand(Hand.MAIN_HAND);

            lastSwapTime = System.currentTimeMillis();
            TripleSwappingMod.LOGGER.debug("Triple swap executed successfully on entity: {}", target.getName().getString());

        } catch (Exception e) {
            TripleSwappingMod.LOGGER.error("Error executing triple swap sequence", e);
        } finally {
            isExecuting = false;
        }
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    public long getLastSwapTime() {
        return lastSwapTime;
    }
}
