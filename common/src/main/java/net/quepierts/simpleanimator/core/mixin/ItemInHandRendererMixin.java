package net.quepierts.simpleanimator.core.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
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

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin implements IModelUpdater {
    @Shadow @Final private EntityRenderDispatcher entityRenderDispatcher;

    @Unique
    public void simpleAnimator$update(@NotNull AbstractClientPlayer pPlayer) {
        ClientAnimator animator = (ClientAnimator) ((IAnimateHandler) pPlayer).simpleanimator$getAnimator();

        if (animator.isRunning()) {
            PlayerRenderer playerrenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(pPlayer);
            PlayerModel<AbstractClientPlayer> model = playerrenderer.getModel();
            float yaw = pPlayer.yHeadRot - pPlayer.yBodyRot;
            float pitch = pPlayer.getXRot();
            model.setupAnim(pPlayer, 0F, 0F, 0F, yaw, pitch);
        }
    }
}
