package net.quepierts.simpleanimator.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.ModelBone;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private Camera mainCamera;

    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void applyCameraRotation(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        if (Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON)
            return;

        ClientAnimator animator = SimpleAnimator.getClient().getClientAnimatorManager().getLocalAnimator();

        if (animator.isRunning() && animator.getAnimation().isOverride() && animator.isProcessed()) {
            ClientAnimator.Cache root = animator.getCache(ModelBone.ROOT);
            ClientAnimator.Cache head = animator.getCache(ModelBone.HEAD);

            LocalPlayer player = Minecraft.getInstance().player;
            assert player != null;
            float yRot = player.yHeadRot - player.yBodyRot;
            float xRot = player.getXRot();

            float x = (float) Math.toDegrees(root.rotation().x + head.rotation().x);
            float y = (float) Math.toDegrees(root.rotation().y + head.rotation().y);

            Camera camera = this.mainCamera;
            ((CameraAccessor) camera).simpleanimator$setRotation(
                    camera.getYRot() + y - yRot,
                    camera.getXRot() + x - xRot
            );

            poseStack.mulPose(Axis.ZP.rotation(root.rotation().z + head.rotation().z));
        }

    }
}
