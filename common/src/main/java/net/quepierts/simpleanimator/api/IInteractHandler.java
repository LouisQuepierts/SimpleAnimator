package net.quepierts.simpleanimator.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.animation.RequestHolder;
import org.jetbrains.annotations.NotNull;

// TODO statalization interaction
@SuppressWarnings("unused")
public interface IInteractHandler {
    boolean simpleanimator$inviteInteract(@NotNull Player target, @NotNull ResourceLocation interact, boolean update);
    boolean simpleanimator$acceptInteract(@NotNull Player requester, boolean update, boolean forced);
    
    void simpleanimator$cancelInteract(boolean update);
    
    boolean simpleanimator$hasRequest();

    @NotNull
    RequestHolder simpleanimator$getRequest();
}
