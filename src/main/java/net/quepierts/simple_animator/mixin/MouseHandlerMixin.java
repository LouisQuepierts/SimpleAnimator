package net.quepierts.simple_animator.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    /*@Shadow private double accumulatedDX;

    @Shadow private double accumulatedDY;

    @Inject(
            method = "onMove",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V"
            ),
            cancellable = true
    )
    public void limit(long pWindowPointer, double pXpos, double pYpos, CallbackInfo ci) {
        if (Minecraft.getInstance().level == null)
            return;

        Animator animator = AnimatorManager.getLocalAnimator();
        if (animator.isRunning() && animator.getAnimation().isOverride()) {
            if (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
                this.accumulatedDX = 0;
                this.accumulatedDY = 0;
                ci.cancel();
            }
        }
    }*/
}
