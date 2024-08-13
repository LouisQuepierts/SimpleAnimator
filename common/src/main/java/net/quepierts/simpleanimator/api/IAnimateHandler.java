package net.quepierts.simpleanimator.api;

import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.core.animation.Animator;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface IAnimateHandler {
    boolean simpleanimator$isRunning();

    boolean simpleanimator$playAnimate(@NotNull ResourceLocation location, boolean update);

    boolean simpleanimator$stopAnimate(boolean update);

    @NotNull
    Animator simpleanimator$getAnimator();
}
