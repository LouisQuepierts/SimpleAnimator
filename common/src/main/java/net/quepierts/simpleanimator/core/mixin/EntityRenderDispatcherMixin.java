package net.quepierts.simpleanimator.core.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.animation.ModelBone;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    public <E extends Entity> void applyRootTranslation(E entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (PlayerUtils.isRiding(entity))
            return;

        if (entity instanceof IAnimateHandler handler) {
            ClientAnimator animator = (ClientAnimator) handler.simpleanimator$getAnimator();

            if (animator.isRunning() && animator.isProcessed()) {
                ClientAnimator.Cache root = animator.getCache(ModelBone.ROOT);
                Vec3 position = PlayerUtils.getRelativePosition((Player) entity, root.position().z, root.position().x);

                poseStack.translate(
                        position.x / -16f,
                        0,  // don't move shadow y
                        position.z / -16f
                );
            }
        }
    }
}
