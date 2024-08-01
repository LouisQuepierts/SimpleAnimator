package net.quepierts.simple_animator.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.client.ClientAnimator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER
            )
    )
    public void update(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo ci) {
        if (!pStack.isEmpty()) {
            ClientAnimator animator = SimpleAnimator.getInstance().getClient().getClientAnimatorManager().getAnimator(pPlayer.getUUID());

            if (animator != null && animator.isRunning()) {
                PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(pPlayer);
                PlayerModel<AbstractClientPlayer> model = playerrenderer.getModel();
                float yaw = pPlayer.yHeadRot - pPlayer.yBodyRot;
                float pitch = pPlayer.getXRot();
                model.setupAnim(pPlayer, 0.0F, 0.0F, 0.0F, yaw, pitch);
            }
        }
    }
}
