package net.quepierts.simpleanimator.core.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.IInteractHandler;
import net.quepierts.simpleanimator.api.animation.Animation;
import net.quepierts.simpleanimator.api.animation.ModelBone;
import net.quepierts.simpleanimator.api.animation.RequestHolder;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.client.ClientPlayerNavigator;
import net.quepierts.simpleanimator.core.client.InputHelper;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Shadow public ClientInput input;

    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/ClientInput;tick()V",
                    shift = At.Shift.AFTER
            )
    )
    public void simpleanimator$limitMove(CallbackInfo ci) {
        ClientProxy client = SimpleAnimator.getClient();

        boolean hasInput = input.forwardImpulse != 0 || input.leftImpulse != 0 || input.keyPresses.jump() || input.keyPresses.shift();

        if (hasInput) {
            final ClientPlayerNavigator navigator = client.getNavigator();
            if (navigator.isNavigating()) {
                navigator.stop(false);
            }

            LocalPlayer player = Minecraft.getInstance().player;
            RequestHolder request = ((IInteractHandler) player).simpleanimator$getRequest();

            if (request.hasRequest()) {
                ((IInteractHandler) player).simpleanimator$cancelInteract(true);
                InputHelper.cancel(input);
                return;
            }

            ClientAnimator animator = (ClientAnimator) ((IAnimateHandler) player).simpleanimator$getAnimator();

            if (animator.isRunning()) {
                Animation animation = animator.getAnimation();
                if (!animation.isMovable()) {
                    InputHelper.cancel(input);
                } else {
                    if (animation.isOverride(ModelBone.BODY)) {
                        input.keyPresses = new Input(
                                input.keyPresses.forward(),
                                input.keyPresses.backward(),
                                input.keyPresses.left(),
                                input.keyPresses.right(),
                                input.keyPresses.jump(),
                                false,
                                input.keyPresses.sprint()
                        );
                    }

                    if (animation.isOverride(ModelBone.LEFT_LEG) || animation.isOverride(ModelBone.RIGHT_LEG)) {
                        InputHelper.idle(input);
                    }
                }

                if (animation.isAbortable() && animator.canStop()) {
                    ((IAnimateHandler) player).simpleanimator$stopAnimate(true);
                }
            }
        }
    }
}
