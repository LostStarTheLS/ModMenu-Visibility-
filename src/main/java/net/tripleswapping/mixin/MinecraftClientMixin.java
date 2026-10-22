package net.tripleswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.tripleswapping.config.TripleSwappingConfig;
import net.tripleswapping.core.TripleSwapManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Shadow
    public ClientPlayerEntity player;

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        TripleSwappingConfig config = TripleSwappingConfig.get();

        if (config.enabled && (config.triggerMode == TripleSwappingConfig.TriggerMode.ON_ATTACK
                || config.triggerMode == TripleSwappingConfig.TriggerMode.OUT_OF_RANGE_ONLY)) {

            // Attempt to trigger the triple swap attack sequence
            if (TripleSwapManager.getInstance().tryExecuteTripleSwap(client, true)) {
                cir.setReturnValue(true);
            }
        }
    }
}
