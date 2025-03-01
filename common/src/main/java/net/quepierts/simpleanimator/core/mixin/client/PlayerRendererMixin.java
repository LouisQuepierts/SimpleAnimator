package net.quepierts.simpleanimator.core.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.quepierts.simpleanimator.core.client.util.PlayerHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    public PlayerRendererMixin(EntityRendererProvider.Context pContext, PlayerModel pModel, float pShadowRadius) {
        super(pContext, pModel, pShadowRadius);
    }

    /*@Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/PlayerModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    public void forceSetupAnimWithRotation(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, ResourceLocation resourceLocation, ModelPart modelPart, boolean bl, CallbackInfo ci) {
        if (((IAnimateHandler) pPlayer).simpleanimator$getAnimator().isRunning()) {
            pRendererArm.xRot = 0.0F;
            ResourceLocation resourceLocation = pPlayer.getSkin().texture();
            pRendererArm.render(pPoseStack, pBuffer.getBuffer(RenderType.entitySolid(resourceLocation)), pCombinedLight, OverlayTexture.NO_OVERLAY);
            pRendererArmwear.xRot = 0.0F;
            pRendererArmwear.render(pPoseStack, pBuffer.getBuffer(RenderType.entityTranslucent(resourceLocation)), pCombinedLight, OverlayTexture.NO_OVERLAY);
            ci.cancel();
        }
    }*/

    @Inject(
            method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V",
            at = @At("TAIL")
    )
    private void simpleanimator$addUUID(
            AbstractClientPlayer abstractClientPlayer,
            PlayerRenderState playerRenderState,
            float f,
            CallbackInfo ci
    ) {
        ((PlayerHolder) playerRenderState).setPlayer(abstractClientPlayer);
    }
}
