package net.quepierts.simpleanimator.api;

import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.api.animation.Animator;
import org.jetbrains.annotations.NotNull;

public interface IAnimateHandler {
    boolean simpleanimator$isRunning();

    void simpleanimator$playAnimate(@NotNull ResourceLocation location, boolean update);

    void simpleanimator$stopAnimate(boolean update);

    @NotNull
    Animator simpleanimator$getAnimator();
}
