package net.tripleswapping.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.tripleswapping.config.TripleSwappingConfig;
import net.tripleswapping.core.TripleSwapManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {

    @Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        // Prevent duplicate attack triggering while TripleSwapManager is in mid-execution
        if (TripleSwapManager.getInstance().isExecuting()) {
            return;
        }

        TripleSwappingConfig config = TripleSwappingConfig.get();
        if (config.enabled && config.triggerMode == TripleSwappingConfig.TriggerMode.ON_ATTACK) {
            // Can coordinate client-side hit tracking
        }
    }
}
