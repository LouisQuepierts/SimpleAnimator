package net.quepierts.simpleanimator.core.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.client.util.IModelUpdater;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin implements IModelUpdater {
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Unique
    public void simpleAnimator$update(@NotNull AbstractClientPlayer pPlayer) {
        ClientAnimator animator = (ClientAnimator) ((IAnimateHandler) pPlayer).simpleanimator$getAnimator();

        if (animator.isRunning()) {
            PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(pPlayer);
            PlayerModel model = playerrenderer.getModel();
            model.resetPose();
            animator.process(model, pPlayer);
        }
    }

    @Inject(
            method = "renderHandsWithItems",
            at = @At("HEAD")
    )
    public void simpleanimator$apply(float f, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer pPlayer, int i, CallbackInfo ci) {
        simpleAnimator$update(pPlayer);
    }
}
