package net.quepierts.simpleanimator.core.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.core.animation.Animator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow protected abstract Vec3 calculateViewVector(float g, float h);

    @Shadow public abstract Vec3 position();

    @Shadow public abstract Level level();

    @Shadow public abstract float getViewXRot(float f);

    @Shadow public abstract float getViewYRot(float f);

    @Shadow public double xo;

    @Shadow public double yo;

    @Shadow public double zo;

    @Shadow public abstract double getX();

    @Shadow public abstract double getY();

    @Shadow public abstract double getZ();

    @Shadow public abstract float getEyeHeight();

    @Inject(
            method = "turn",
            at = @At("HEAD"),
            cancellable = true
    )
    public void limitTurn(double pYRot, double pXRot, CallbackInfo ci) {
        if ((Object) this != Minecraft.getInstance().player)
            return;

        Animator animator = ((IAnimateHandler) this).simpleanimator$getAnimator();

        if (animator.isRunning() && animator.getAnimation().isOverrideHead())
            ci.cancel();
    }

    @Inject(
            method = "getEyePosition(F)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getEyePositionDuringAnimating(float f, CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this != Minecraft.getInstance().player)
            return;

        ClientAnimator animator = (ClientAnimator) ((IAnimateHandler) this).simpleanimator$getAnimator();
        if (animator.isRunning() && animator.isProcessed() && !animator.getAnimation().isOverrideHead()) {
            Vector3f position = animator.getCameraPosition();
            double d0 = Mth.lerp(f, this.xo, this.getX()) + position.x;
            double d1 = Mth.lerp(f, this.yo, this.getY()) + (double)this.getEyeHeight() + position.y;
            double d2 = Mth.lerp(f, this.zo, this.getZ()) + position.z;
            cir.setReturnValue(new Vec3(d0, d1, d2));
            cir.cancel();
        }
    }

    @Inject(
            method = "getViewVector",
            at = @At("HEAD"),
            cancellable = true
    )
    public void getViewVectorDuringAnimating(float f, CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this != Minecraft.getInstance().player)
            return;

        ClientAnimator animator = (ClientAnimator) ((IAnimateHandler) this).simpleanimator$getAnimator();
        if (animator.isRunning() && animator.isProcessed() && !animator.getAnimation().isOverrideHead()) {
            Vector3f rotation = animator.getCameraRotation();
            cir.setReturnValue(this.calculateViewVector(
                    this.getViewXRot(f) + rotation.x * Mth.RAD_TO_DEG,
                    this.getViewYRot(f) + rotation.y * Mth.RAD_TO_DEG
            ));
            cir.cancel();
        }
    }
}
