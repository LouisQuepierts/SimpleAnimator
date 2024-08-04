package net.quepierts.simpleanimator.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.core.animation.RequestHolder;
import org.jetbrains.annotations.NotNull;

public interface IInteractHandler {
    boolean simpleanimator$invite(@NotNull Player target, @NotNull ResourceLocation interact, boolean update);
    boolean simpleanimator$accept(@NotNull Player requester, boolean update);
    
    void simpleanimator$cancel(boolean update);
    
    boolean simpleanimator$hasRequest();

    @NotNull
    RequestHolder simpleanimator$getRequest();
}
