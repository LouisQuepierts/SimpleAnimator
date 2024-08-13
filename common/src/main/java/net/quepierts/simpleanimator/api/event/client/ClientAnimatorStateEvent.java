package net.quepierts.simpleanimator.api.event.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.api.animation.Animation;
import net.quepierts.simpleanimator.api.animation.AnimationState;
import net.quepierts.simpleanimator.api.event.SAEvent;

import java.util.UUID;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public abstract class ClientAnimatorStateEvent extends SAEvent {
    private final UUID owner;
    private final ResourceLocation animationID;
    private final Animation animation;
    private final AnimationState curState;
    private final AnimationState nextState;
    private final boolean local;

    protected ClientAnimatorStateEvent(UUID owner, ResourceLocation animationID, Animation animation, AnimationState curState, AnimationState nextState) {
        this.owner = owner;
        this.animationID = animationID;
        this.animation = animation;
        this.curState = curState;
        this.nextState = nextState;
        this.local = Minecraft.getInstance().getGameProfile().getId().equals(owner);
    }

    public UUID getOwner() {
        return owner;
    }

    public ResourceLocation getAnimationID() {
        return animationID;
    }

    public Animation getAnimation() {
        return animation;
    }

    public AnimationState getCurState() {
        return curState;
    }

    public AnimationState getNextState() {
        return nextState;
    }

    public boolean isLocal() {
        return local;
    }

    public static class Enter extends ClientAnimatorStateEvent {
        public Enter(UUID owner, ResourceLocation animationID, Animation animation, AnimationState curState, AnimationState nextState) {
            super(owner, animationID, animation, curState, nextState);
        }
    }

    public static class Exit extends ClientAnimatorStateEvent {
        public Exit(UUID owner, ResourceLocation animationID, Animation animation, AnimationState curState, AnimationState nextState) {
            super(owner, animationID, animation, curState, nextState);
        }
    }

    public static class Loop extends ClientAnimatorStateEvent {
        public Loop(UUID owner, ResourceLocation animationID, Animation animation, AnimationState curState, AnimationState nextState) {
            super(owner, animationID, animation, curState, nextState);
        }
    }
}
