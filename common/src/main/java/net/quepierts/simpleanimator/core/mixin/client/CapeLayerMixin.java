package net.quepierts.simpleanimator.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.quepierts.simpleanimator.api.animation.ModelBone;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    @Unique
    private static final ResourceLocation sa$TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");
    public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pRenderer) {
        super(pRenderer);
    }

    /*@Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    public void sa$debug(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClientPlayer pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        ClientAnimator animator = SimpleAnimator.getClient().getClientAnimatorManager().getAnimator(pLivingEntity.getUUID());

        if (animator != null && animator.isRunning()) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.0f, 0.0f, 0.125f);
            this.getParentModel().body.translateAndRotate(pPoseStack);
            pPoseStack.translate(0.0f, 0.0f, 0.0f);
            pPoseStack.mulPose(Axis.XP.rotation(6.0f * Mth.DEG_TO_RAD));
            pPoseStack.mulPose(Axis.YP.rotation(Mth.PI));

            VertexConsumer vertexConsumer = pBuffer.getBuffer(RenderType.entitySolid(sa$TEXTURE));
            this.getParentModel().renderCloak(pPoseStack, vertexConsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
            pPoseStack.popPose();
            ci.cancel();
        }
    }*/

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void simpleanimator$followBody(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClientPlayer pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        ClientAnimator animator = SimpleAnimator.getClient().getClientAnimatorManager().getAnimator(pLivingEntity.getUUID());

        if (animator == null || !animator.isRunning()) {
            return;
        }

        this.getParentModel().body.translateAndRotate(pPoseStack);
    }
}
