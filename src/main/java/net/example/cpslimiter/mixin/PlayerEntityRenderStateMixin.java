package net.example.cpslimiter.mixin;

import net.example.cpslimiter.access.PlayerStateAccessor;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import java.util.UUID;

@Mixin(PlayerEntityRenderState.class)
public class PlayerEntityRenderStateMixin implements PlayerStateAccessor {
    @Unique private UUID cpsLimiter$uuid;

    @Override public void cpslimiter$setUuid(UUID uuid) { this.cpsLimiter$uuid = uuid; }
    @Override public UUID cpslimiter$getUuid() { return this.cpsLimiter$uuid; }
}