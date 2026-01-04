package net.example.cpslimiter.mixin;

import net.example.cpslimiter.access.PlayerStateAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    @Inject(method = "updateRenderState", at = @At("RETURN"))
    private void injectUuidToState(AbstractClientPlayerEntity player, PlayerEntityRenderState state, float f, CallbackInfo ci) {
        // Кастуем СТРОГО к интерфейсу PlayerStateAccessor
        ((PlayerStateAccessor) state).cpslimiter$setUuid(player.getUuid());
    }
}