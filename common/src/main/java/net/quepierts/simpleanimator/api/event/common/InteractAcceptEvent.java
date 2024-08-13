package net.quepierts.simpleanimator.api.event.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.event.ICancelable;
import net.quepierts.simpleanimator.api.event.SAEvent;

@SuppressWarnings("unused")
public abstract class InteractAcceptEvent extends SAEvent {
    private final Player inviter;
    private final Player acceptor;
    private final boolean forced;

    protected InteractAcceptEvent(Player inviter, Player acceptor, boolean forced) {
        this.inviter = inviter;
        this.acceptor = acceptor;
        this.forced = forced;
    }

    public Player getInviter() {
        return inviter;
    }

    public Player getAcceptor() {
        return acceptor;
    }

    public boolean isForced() {
        return forced;
    }

    public static class Pre extends InteractAcceptEvent implements ICancelable {
        public Pre(Player inviter, Player acceptor, boolean forced) {
            super(inviter, acceptor, forced);
        }
    }

    public static class Post extends InteractAcceptEvent {
        private final ResourceLocation interactionID;
        public Post(Player inviter, Player acceptor, boolean forced, ResourceLocation interactionID) {
            super(inviter, acceptor, forced);
            this.interactionID = interactionID;
        }

        public ResourceLocation getInteractionID() {
            return interactionID;
        }
    }
}
