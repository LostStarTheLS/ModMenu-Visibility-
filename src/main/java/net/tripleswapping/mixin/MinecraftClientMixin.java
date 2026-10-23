package net.tripleswapping.mixin;

import net.minecraft.client.MinecraftClient;
import net.tripleswapping.core.TripleSwapManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

    /**
     * Intercepts left-click attack initiation in Minecraft 1.21.11 before vanilla checks
     * {@code DataComponentTypes.PIERCING_WEAPON} (Spear {@code STAB}) or standard {@code crosshairTarget}.
     * Enables hitting targets inside the 3.0–4.5m Spear extended reach window seamlessly on left-click.
     */
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (client.player == null || client.world == null) {
            return;
        }

        if (TripleSwapManager.getInstance().handleClientAttackAttempt(client)) {
            cir.setReturnValue(true);
        }
    }
}
