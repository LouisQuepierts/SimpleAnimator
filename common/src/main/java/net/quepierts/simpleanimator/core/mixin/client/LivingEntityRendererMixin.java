package net.quepierts.simpleanimator.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.quepierts.simpleanimator.api.animation.ModelBone;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Unique @Nullable ClientAnimator animator;
    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                    ordinal = 1
            )
    )
    public void translateRoot(T pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, CallbackInfo ci) {
        if (PlayerUtils.isRiding(pEntity))
            return;

        animator = SimpleAnimator.getClient().getClientAnimatorManager().getAnimator(pEntity.getUUID());
        if (animator != null && animator.isRunning() && animator.isProcessed()) {
            ClientAnimator.Cache root = animator.getCache(ModelBone.ROOT);
            pPoseStack.mulPose(new Quaternionf().rotationXYZ(
                    root.rotation().x,
                    root.rotation().y,
                    root.rotation().z
            ));
            pPoseStack.translate(0f, root.position().y / -16.0f, 0f);
        }
    }

    /*@Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"
            )
    )
    public void redirectRenderCall(EntityModel<?> model, PoseStack poseStack, VertexConsumer consumer, int i, int j, int k) {
        if (animator != null && animator.isRunning() && model instanceof IModifiedModel modifiedModel) {
            modifiedModel.simpleAnimator$render(poseStack, consumer, animator, animator.getAnimation().isModifiedRig(), i, j, k);
            return;
        }

        model.renderToBuffer(poseStack, consumer, i, j, k);
    }*/
}
