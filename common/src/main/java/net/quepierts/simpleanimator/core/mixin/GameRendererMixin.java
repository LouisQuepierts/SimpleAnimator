package net.quepierts.simpleanimator.core.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.client.util.IModelUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
