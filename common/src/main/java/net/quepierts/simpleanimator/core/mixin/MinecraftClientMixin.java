package net.quepierts.simpleanimator.core.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.mixin.accessor.KeyMappingAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Shadow @Final public Options options;

    @Shadow @Nullable
    public LocalPlayer player;

    @Shadow @Final private DeltaTracker.Timer timer;

    @Shadow public abstract boolean isPaused();

    @Shadow @Nullable public ClientLevel level;

    @Inject(
            method = "handleKeybinds",
            at = @At("HEAD")
    )
    @SuppressWarnings("all")
    public void cancelKeyInput(CallbackInfo ci) {
        Animator animator = ((IAnimateHandler) this.player).simpleanimator$getAnimator();

        if (animator.isRunning() && animator.getAnimation().isOverride()) {
            ((KeyMappingAccessor) this.options.keyUse).simpleanimator$release();
            ((KeyMappingAccessor) this.options.keyAttack).simpleanimator$release();
        }
    }

    @Inject(
            method = "runTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/DeltaTracker$Timer;advanceTime(JZ)I",
                    shift = At.Shift.AFTER
            )
    )
    public void tickAnimators(boolean bl, CallbackInfo ci) {
        if (!this.isPaused() && level != null) {
            SimpleAnimator.getClient().getAnimatorManager().tick(timer.getGameTimeDeltaTicks() / 20);
        }
    }
}
