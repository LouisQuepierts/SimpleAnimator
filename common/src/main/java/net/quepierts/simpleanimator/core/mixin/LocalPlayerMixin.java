package net.quepierts.simpleanimator.core.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.IInteractHandler;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.RequestHolder;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.client.ClientPlayerNavigator;
import net.quepierts.simpleanimator.core.network.packet.AnimatorStopPacket;
import net.quepierts.simpleanimator.core.network.packet.InteractCancelPacket;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

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
            final UUID uuid = ((LocalPlayer) (Object) this).getUUID();
            final ClientPlayerNavigator navigator = client.getNavigator();
            if (navigator.isNavigating()) {
                navigator.stop();
            }

            IInteractHandler handler = (IInteractHandler) Minecraft.getInstance().player;
            RequestHolder request = handler.simpleanimator$getRequest();

            if (request.hasRequest()) {
                handler.simpleanimator$cancel(true);
                SimpleAnimator.getNetwork().update(new InteractCancelPacket(uuid));
                return;
            }

            ClientAnimator animator = (ClientAnimator) ((IAnimateHandler) Minecraft.getInstance().player).simpleanimator$getAnimator();

            if (animator.canStop() && !animator.getAnimation().isMovable()) {
                if (animator.getAnimation().isAbortable()) {
                    animator.stop();
                    SimpleAnimator.getNetwork().update(new AnimatorStopPacket(uuid));
                }

                input.forwardImpulse = 0.0f;
                input.leftImpulse = 0.0f;
                input.jumping = false;
                input.shiftKeyDown = false;
            }
        }
    }
}
