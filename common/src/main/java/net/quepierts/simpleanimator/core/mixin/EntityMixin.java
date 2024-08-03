package net.quepierts.simpleanimator.core.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public class EntityMixin {
    @Shadow protected UUID uuid;

    @Inject(
            method = "turn",
            at = @At("HEAD"),
            cancellable = true
    )
    public void limitTurn(double pYRot, double pXRot, CallbackInfo ci) {
        if (!uuid.equals(Minecraft.getInstance().player.getUUID()))
            return;

        ClientAnimator animator = SimpleAnimator.getClient().getClientAnimatorManager().getLocalAnimator();

        if (animator.isRunning() && animator.getAnimation().isOverride())
            ci.cancel();
    }
}
