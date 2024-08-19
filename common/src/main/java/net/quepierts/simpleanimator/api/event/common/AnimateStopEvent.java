package net.quepierts.simpleanimator.api.event.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.event.ICancelable;
import net.quepierts.simpleanimator.api.event.SAEvent;

@SuppressWarnings("unused")
public abstract class AnimateStopEvent extends SAEvent {
    private final Player player;
    private final ResourceLocation animationID;

    protected AnimateStopEvent(Player player, ResourceLocation animationID) {
        this.player = player;
        this.animationID = animationID;
    }

    public ResourceLocation getAnimationID() {
        return animationID;
    }

    public Player getPlayer() {
        return player;
    }

    public static class Pre extends AnimatePlayEvent implements ICancelable {
        public Pre(Player player, ResourceLocation animationID) {
            super(player, animationID);
        }
    }

    public static class Post extends AnimatePlayEvent {
        public Post(Player player, ResourceLocation animationID) {
            super(player, animationID);
        }
    }
}
