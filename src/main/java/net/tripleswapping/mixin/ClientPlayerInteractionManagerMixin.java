package net.tripleswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.tripleswapping.config.TripleSwappingConfig;
import net.tripleswapping.core.RaycastHelper;
import net.tripleswapping.core.TripleSwapManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    /**
     * Intercepts direct entity attacks within normal melee range when {@code ON_ATTACK} or {@code HOLD_KEY}
     * trigger mode is active, routing them through the 1.21.11 Triple Swap packet sequence.
     */
    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || player != client.player) {
            return;
        }

        TripleSwapManager manager = TripleSwapManager.getInstance();
        if (manager.isSwapping()) {
            return;
        }

        TripleSwappingConfig config = TripleSwappingConfig.getInstance();
        if (!config.enabled) {
            return;
        }

        boolean shouldSwap = false;
        if (config.triggerMode == TripleSwappingConfig.TriggerMode.ON_ATTACK) {
            shouldSwap = true;
        } else if (config.triggerMode == TripleSwappingConfig.TriggerMode.OUT_OF_NORMAL_RANGE) {
            shouldSwap = RaycastHelper.isOutOfNormalRange(client, target);
        }

        if (shouldSwap && RaycastHelper.isAttackableTarget(target)) {
            if (manager.executeTripleSwapAttack(client, target)) {
                ci.cancel();
            }
        }
    }
}
