package net.quepierts.simple_animator.core.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.animation.ModelBone;
import net.quepierts.simple_animator.core.client.ClientAnimator;
import net.quepierts.simple_animator.core.client.ClientAnimatorManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow protected abstract void setPosition(double pX, double pY, double pZ);

    @Shadow private boolean detached;

    @Shadow private Vec3 position;

    @Inject(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    public void applyAnimation(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {
        if (detached)
            return;

        ClientAnimator animator = ((ClientAnimatorManager) SimpleAnimator.getInstance().getProxy().getAnimatorManager()).getLocalAnimator();

        if (animator.isRunning()) {
            ClientAnimator.Cache root = animator.getCache(ModelBone.ROOT);
            ClientAnimator.Cache head = animator.getCache(ModelBone.HEAD);
            this.setPosition(
                    (root.position().x + head.position().x) / 16.0f + position.x,
                    (root.position().y + head.position().y) / 16.0f + position.y,
                    (root.position().z + head.position().z) / 16.0f + position.z
            );
            ci.cancel();
        }
    }
}
