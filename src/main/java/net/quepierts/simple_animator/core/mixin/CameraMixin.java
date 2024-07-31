package net.quepierts.simple_animator.core.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.client.ClientAnimator;
import net.quepierts.simple_animator.core.common.animation.ModelBone;
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

        ClientAnimator animator = SimpleAnimator.getInstance().getClient().getClientAnimatorManager().getLocalAnimator();

        if (animator.isRunning()) {
            ClientAnimator.Cache root = animator.getCache(ModelBone.ROOT);
            ClientAnimator.Cache head = animator.getCache(ModelBone.HEAD);

            final float left = (root.position().x + head.position().x) / -16.0f;
            final float up = (root.position().y + head.position().y) / 16.0f;
            final float forward = (root.position().z + head.position().z) / -16.0f;

            LocalPlayer player = Minecraft.getInstance().player;
            Vec2 vec2 = new Vec2(0, player.yBodyRot);
            float f = Mth.cos((vec2.y + 90.0F) * ((float)Math.PI / 180F));
            float f1 = Mth.sin((vec2.y + 90.0F) * ((float)Math.PI / 180F));
            Vec3 vec31 = new Vec3(f, 0, f1);
            Vec3 vec33 = new Vec3(-f1, 0, f);
            double d0 = vec31.x * forward + vec33.x * left;
            double d2 = vec31.z * forward + vec33.z * left;

            this.setPosition(
                    this.position.x + d0,
                    this.position.y + up,
                    this.position.z + d2);
            ci.cancel();
        }
    }
}
