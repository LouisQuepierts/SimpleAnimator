package net.quepierts.simpleanimator.api.event.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.event.ICancelable;
import net.quepierts.simpleanimator.api.event.SAEvent;

@SuppressWarnings("unused")
public abstract class InteractInviteEvent extends SAEvent {
    private final Player inviter;
    private final Player target;
    private final ResourceLocation interactionID;

    protected InteractInviteEvent(Player inviter, Player target, ResourceLocation interactionID) {
        this.inviter = inviter;
        this.target = target;
        this.interactionID = interactionID;
    }

    public Player getInviter() {
        return inviter;
    }

    public Player getTarget() {
        return target;
    }

    public ResourceLocation getInteractionID() {
        return interactionID;
    }

    public static class Pre extends InteractInviteEvent implements ICancelable {
        public Pre(Player inviter, Player target, ResourceLocation interactionID) {
            super(inviter, target, interactionID);
        }
    }

    public static class Post extends InteractInviteEvent {
        public Post(Player inviter, Player target, ResourceLocation interactionID) {
            super(inviter, target, interactionID);
        }
    }
}
