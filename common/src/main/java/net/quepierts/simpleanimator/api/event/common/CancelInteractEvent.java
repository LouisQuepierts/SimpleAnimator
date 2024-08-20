package net.quepierts.simpleanimator.api.event.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.event.SAEvent;

import java.util.UUID;

@SuppressWarnings("unused")
public class CancelInteractEvent extends SAEvent {
    private final Player player;
    private final UUID target;
    private final ResourceLocation interactionID;

    public CancelInteractEvent(Player player, UUID other, ResourceLocation interactionID) {
        this.player = player;
        this.target = other;
        this.interactionID = interactionID;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getTarget() {
        return target;
    }

    public ResourceLocation getInteractionID() {
        return interactionID;
    }
}
