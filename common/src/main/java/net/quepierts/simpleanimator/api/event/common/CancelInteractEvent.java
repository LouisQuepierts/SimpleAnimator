package net.quepierts.simpleanimator.api.event.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.event.SAEvent;

@SuppressWarnings("unused")
public class CancelInteractEvent extends SAEvent {
    private final Player player;
    private final ResourceLocation interactionID;

    public CancelInteractEvent(Player player, ResourceLocation interactionID) {
        this.player = player;
        this.interactionID = interactionID;
    }

    public Player getPlayer() {
        return player;
    }

    public ResourceLocation getInteractionID() {
        return interactionID;
    }
}
