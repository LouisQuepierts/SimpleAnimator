package net.quepierts.simpleanimator.core.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow protected abstract void setPosition(double pX, double pY, double pZ);
    @Shadow protected abstract void setRotation(float f, float g);
    @Shadow public abstract Quaternionf rotation();

    @Shadow private Vec3 position;
    @Shadow private float xRot;
    @Shadow private float yRot;

    @Inject(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
                    shift = At.Shift.AFTER
            )
    )
    public void applyAnimation(BlockGetter pLevel, Entity pEntity, boolean pDetached, boolean pThirdPersonReverse, float pPartialTick, CallbackInfo ci) {
        ClientAnimator animator = SimpleAnimator.getClient().getClientAnimatorManager().getLocalAnimator();

        if (animator.isRunning() && animator.isProcessed()) {
            Vector3f position = animator.getCameraPosition(pEntity);

            LocalPlayer player = Minecraft.getInstance().player;
            Vec2 vec2 = new Vec2(0, player.yBodyRot);
            float f = Mth.cos((vec2.y + 90.0F) * ((float)Math.PI / 180F));
            float f1 = Mth.sin((vec2.y + 90.0F) * ((float)Math.PI / 180F));
            Vec3 vec31 = new Vec3(f, 0, f1);
            Vec3 vec33 = new Vec3(-f1, 0, f);
            double d0 = vec31.x * position.z + vec33.x * position.x;
            double d2 = vec31.z * position.z + vec33.z * position.x;

            this.setPosition(
                    this.position.x + d0,
                    this.position.y + position.y,
                    this.position.z + d2);

            if (animator.getAnimation().isOverride() && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                Vector3f rotation = animator.getCameraRotation();

                float yRot = (player.yHeadRot - player.yBodyRot);
                float xRot = player.getXRot();

                this.setRotation(
                        this.yRot + (rotation.y * Mth.RAD_TO_DEG) - yRot,
                        this.xRot + (rotation.x * Mth.RAD_TO_DEG) - xRot
                );

                this.rotation().rotateZ(rotation.z);
            }
        }
    }
}
