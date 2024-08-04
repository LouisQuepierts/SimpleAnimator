package net.quepierts.simpleanimator.api.animation;

import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.AnimationState;
import net.quepierts.simpleanimator.core.network.packet.AnimatorDataPacket;

import java.util.UUID;

public class Animator {
    public static final ResourceLocation EMPTY = new ResourceLocation("empty");
    protected final UUID uuid;

    protected ResourceLocation animationLocation;
    protected Animation animation;

    protected AnimationState curState;
    protected AnimationState nextState;
    protected ProcessState procState;
    protected float timer;
    protected float speed = 1.0f;

    public Animator(UUID uuid) {
        this.uuid = uuid;
        this.animationLocation = EMPTY;
        this.curState = AnimationState.IDLE;
        this.nextState = AnimationState.IDLE;
        this.procState = ProcessState.PROCESS;
        this.timer = 0.0f;
    }

    public void sync(AnimatorDataPacket packet) {
        SimpleAnimator.LOGGER.debug("Update: {} {} ,", this.getClass().getSimpleName(), packet);
        this.animationLocation = packet.animationLocation;
        this.animation = SimpleAnimator.getProxy().getAnimationManager().getAnimation(packet.animationLocation);
        this.curState = packet.curState;
        this.nextState = packet.nextState;
        this.procState = packet.procState;
        this.timer = packet.timer;
        this.speed = packet.speed;
    }

    public boolean play(ResourceLocation location) {
        if (this.animation != null && !this.animation.isAbortable())
            return false;

        this.animationLocation = location;
        this.animation = SimpleAnimator.getProxy().getAnimationManager().getAnimation(location);
        this.timer = 0;
        return true;
    }

    public boolean stop() {
        if (this.animation == null || !this.animation.isAbortable())
            return false;
        this.timer = 0;
        return true;
    }

    public void terminate() {
        this.animationLocation = EMPTY;
        this.timer = 0;
    }

    public UUID getUuid() {
        return uuid;
    }

    public ResourceLocation getAnimationLocation() {
        return animationLocation;
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

    public ProcessState getProcState() {
        return procState;
    }

    public float getTimer() {
        return timer;
    }

    public void reset(boolean update) {
        this.timer = 0;
        this.animation = null;
        this.animationLocation = EMPTY;
    }

    public boolean isRunning() {
        return !animationLocation.equals(EMPTY) && this.animation != null;
    }

    public float getSpeed() {
        return speed;
    }

    public enum ProcessState {
        TRANSFER,
        PROCESS
    }
}
