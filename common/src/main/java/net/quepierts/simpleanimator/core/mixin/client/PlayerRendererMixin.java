package net.quepierts.simpleanimator.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerRendererMixin(EntityRendererProvider.Context pContext, PlayerModel<AbstractClientPlayer> pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }

    @Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/PlayerModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    public void forceSetupAnimWithRotation(PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, AbstractClientPlayer pPlayer, ModelPart pRendererArm, ModelPart pRendererArmwear, CallbackInfo ci) {
        if (((IAnimateHandler) pPlayer).simpleanimator$getAnimator().isRunning()) {
            pRendererArm.xRot = 0.0F;
            ResourceLocation resourceLocation = pPlayer.getSkin().texture();
            pRendererArm.render(pPoseStack, pBuffer.getBuffer(RenderType.entitySolid(resourceLocation)), pCombinedLight, OverlayTexture.NO_OVERLAY);
            pRendererArmwear.xRot = 0.0F;
            pRendererArmwear.render(pPoseStack, pBuffer.getBuffer(RenderType.entityTranslucent(resourceLocation)), pCombinedLight, OverlayTexture.NO_OVERLAY);
            ci.cancel();
        }
    }
}
