package net.example.cpslimiter.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class CpsLimiterMixin {
    @Shadow private int attackCooldown;

    @Unique private long lastAttackTime = 0L;
    // 50_000_000 наносекунд = 50 миллисекунд (ровно 20 CPS)
    @Unique private static final long LIMIT_NS = 50_000_000L; 

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void limitAttackRate(CallbackInfoReturnable<Boolean> cir) {
        long now = System.nanoTime();
        
        if (now - lastAttackTime < LIMIT_NS) {
            cir.setReturnValue(false);
        } else {
            lastAttackTime = now;
            this.attackCooldown = 0;
        }
    }

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onInput(CallbackInfo ci) {
        this.attackCooldown = 0;
    }
}