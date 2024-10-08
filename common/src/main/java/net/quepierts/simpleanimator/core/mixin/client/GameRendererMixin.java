package net.quepierts.simpleanimator.core.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.core.animation.Animator;
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
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            ),
            cancellable = true
    )
    public void simpleanimator$dontRenderBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (this.minecraft.player == null)
            return;

        ((IModelUpdater) this.itemInHandRenderer).simpleAnimator$update(this.minecraft.player);

        Animator animator = ((IAnimateHandler) this.minecraft.player).simpleanimator$getAnimator();
        if (animator.isRunning() && animator.getAnimation().isOverrideHead()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
