package net.quepierts.simple_animator.animation;

import net.minecraft.resources.ResourceLocation;
import net.quepierts.simple_animator.SimpleAnimator;
import net.quepierts.simple_animator.network.packet.AnimatorPacket;

import java.util.UUID;

public class Animator {
    public static final ResourceLocation EMPTY = new ResourceLocation("empty");
    protected final UUID uuid;

    protected ResourceLocation animationLocation;

    protected AnimationState curState;
    protected AnimationState nextState;
    protected ProcessState procState;
    protected float timer;

    public Animator(UUID uuid) {
        this.uuid = uuid;
        this.animationLocation = EMPTY;
        this.curState = AnimationState.IDLE;
        this.nextState = AnimationState.IDLE;
        this.procState = ProcessState.PROCESS;
        this.timer = 0.0f;
    }

    public void sync(AnimatorPacket packet) {
        SimpleAnimator.LOGGER.info("Update: {} {} ,", this.getClass().getSimpleName(), packet);
        this.animationLocation = packet.animationLocation;
        this.curState = packet.curState;
        this.nextState = packet.nextState;
        this.procState = packet.procState;
        this.timer = packet.timer;
    }

    public void play(ResourceLocation location) {
        this.animationLocation = location;
        this.timer = 0;
    }

    public void stop() {
        this.timer = 0;
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

    public void reset() {
        this.timer = 0;
        this.animationLocation = EMPTY;
    }

    public boolean isRunning() {
        return !animationLocation.equals(EMPTY);
    }

    public enum ProcessState {
        TRANSFER,
        PROCESS
    }
}
