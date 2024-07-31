package net.quepierts.simple_animator.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.common.animation.ModelBone;
import net.quepierts.simple_animator.core.client.ClientAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public CapeLayerMixin(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> pRenderer) {
        super(pRenderer);
    }

    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    public void process(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, AbstractClientPlayer pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch, CallbackInfo ci) {
        ClientAnimator animator = SimpleAnimator.getInstance().getClient().getClientAnimatorManager().getAnimator(pLivingEntity.getUUID());

        if (animator == null || !animator.isRunning()) {
            return;
        }

        ClientAnimator.Cache cache = animator.getCache(ModelBone.BODY);

        pPoseStack.translate(cache.position().x / 16.0f, cache.position().y / 16.0f, cache.position().z / 16.0f + 0.125f);
        pPoseStack.mulPose(Axis.XP.rotation((float) (cache.rotation().x + Math.toRadians(6.0f))));
        pPoseStack.mulPose(Axis.ZP.rotation(cache.rotation().y));
        pPoseStack.mulPose(Axis.YP.rotationDegrees((float) (180.0f - Math.toDegrees(cache.rotation().z))));

        VertexConsumer vertexconsumer = pBuffer.getBuffer(RenderType.entitySolid(pLivingEntity.getCloakTextureLocation()));
        this.getParentModel().renderCloak(pPoseStack, vertexconsumer, pPackedLight, OverlayTexture.NO_OVERLAY);
        pPoseStack.popPose();

        ci.cancel();
    }
}
