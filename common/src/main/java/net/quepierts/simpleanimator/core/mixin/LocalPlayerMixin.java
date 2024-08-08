package net.quepierts.simpleanimator.core.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.IInteractHandler;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.RequestHolder;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.client.ClientPlayerNavigator;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Shadow public Input input;

    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/Input;tick(ZF)V",
                    shift = At.Shift.AFTER
            )
    )
    public void simpleanimator$limitMove(CallbackInfo ci) {
        ClientProxy client = SimpleAnimator.getClient();

        boolean hasInput = input.forwardImpulse != 0 || input.leftImpulse != 0 || input.jumping || input.shiftKeyDown;

        if (hasInput) {
            final ClientPlayerNavigator navigator = client.getNavigator();
            if (navigator.isNavigating()) {
                navigator.stop();
            }

            LocalPlayer player = Minecraft.getInstance().player;
            RequestHolder request = ((IInteractHandler) player).simpleanimator$getRequest();

            if (request.hasRequest()) {
                ((IInteractHandler) player).simpleanimator$cancel(true);

                input.forwardImpulse = 0.0f;
                input.leftImpulse = 0.0f;
                input.jumping = false;
                input.shiftKeyDown = false;
                return;
            }

            ClientAnimator animator = (ClientAnimator) ((IAnimateHandler) player).simpleanimator$getAnimator();

            if (animator.isRunning() && !animator.getAnimation().isMovable()) {
                if (animator.getAnimation().isAbortable() && animator.canStop()) {
                    ((IAnimateHandler) player).simpleanimator$stopAnimate(true);
                }

                input.forwardImpulse = 0.0f;
                input.leftImpulse = 0.0f;
                input.jumping = false;
                input.shiftKeyDown = false;
            }
        }
    }
}
