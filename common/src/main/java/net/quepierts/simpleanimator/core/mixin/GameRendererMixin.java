package net.quepierts.simpleanimator.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.client.util.IModelUpdater;
import net.quepierts.simpleanimator.core.mixin.accessor.CameraAccessor;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("all")
@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Camera mainCamera;

    @Shadow @Final public ItemInHandRenderer itemInHandRenderer;

    @Shadow @Final Minecraft minecraft;

    @Inject(
            method = "shouldRenderBlockOutline",
            at = @At("HEAD"),
            cancellable = true
    )
    public void dontRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        ((IModelUpdater) this.itemInHandRenderer).simpleAnimator$update(this.minecraft.player);

        Animator animator = ((IAnimateHandler) this.minecraft.player).simpleanimator$getAnimator();
        if (animator.isRunning() && animator.getAnimation().isOverride()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void applyCameraRotation(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        if (this.minecraft.options.getCameraType() != CameraType.FIRST_PERSON)
            return;

        ((IModelUpdater) this.itemInHandRenderer).simpleAnimator$update(this.minecraft.player);

        ClientAnimator animator = SimpleAnimator.getClient().getClientAnimatorManager().getLocalAnimator();

        if (animator.isRunning() && animator.getAnimation().isOverride() && animator.isProcessed()) {
            Vector3f rotation = animator.getCameraRotation().mul(Mth.RAD_TO_DEG);

            LocalPlayer player = this.minecraft.player;
            float yRot = player.yHeadRot - player.yBodyRot;
            float xRot = player.getXRot();


            Camera camera = this.mainCamera;
            ((CameraAccessor) camera).simpleanimator$setRotation(
                    camera.getYRot() + rotation.y - yRot,
                    camera.getXRot() + rotation.x - xRot
            );

            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation.z));
        }
    }
}
