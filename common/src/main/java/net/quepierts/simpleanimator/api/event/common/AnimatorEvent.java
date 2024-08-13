package net.quepierts.simpleanimator.api.event.common;

import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.api.animation.Animation;
import net.quepierts.simpleanimator.api.event.SAEvent;

import java.util.UUID;

@SuppressWarnings("unused")
public abstract class AnimatorEvent extends SAEvent {
    private final UUID owner;
    private final ResourceLocation animationID;
    private final Animation animation;

    public UUID getOwner() {
        return owner;
    }

    public ResourceLocation getAnimationID() {
        return animationID;
    }

    public Animation getAnimation() {
        return animation;
    }

    protected AnimatorEvent(UUID owner, ResourceLocation animationID, Animation animation) {
        this.owner = owner;
        this.animationID = animationID;
        this.animation = animation;
    }

    public static class Play extends AnimatorEvent {
        public Play(UUID owner, ResourceLocation animationID, Animation animation) {
            super(owner, animationID, animation);
        }
    }

    public static class Stop extends AnimatorEvent {
        public Stop(UUID owner, ResourceLocation animationID, Animation animation) {
            super(owner, animationID, animation);
        }
    }

    public static class Reset extends AnimatorEvent {
        public Reset(UUID owner, ResourceLocation animationID, Animation animation) {
            super(owner, animationID, animation);
        }
    }
}
