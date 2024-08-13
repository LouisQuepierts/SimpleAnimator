package net.quepierts.simpleanimator.core.animation;

import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.api.animation.Animation;
import net.quepierts.simpleanimator.api.animation.AnimationState;
import net.quepierts.simpleanimator.api.event.common.AnimatorEvent;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.packet.AnimatorDataPacket;

import java.util.UUID;

public class Animator {
    public static final ResourceLocation EMPTY = ResourceLocation.fromNamespaceAndPath(SimpleAnimator.MOD_ID, "empty");
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

        SimpleAnimator.EVENT_BUS.post(new AnimatorEvent.Play(this.uuid, this.animationLocation, this.animation));
        return true;
    }

    public boolean stop() {
        if (this.animation == null)
            return false;

        SimpleAnimator.EVENT_BUS.post(new AnimatorEvent.Stop(this.uuid, this.animationLocation, this.animation));
        /*RequestHolder holder = SimpleAnimator.getProxy().getInteractionManager().get(this.uuid);
        if (holder != null) {
            holder.reset();
        }*/
        this.timer = 0;
        return true;
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
        SimpleAnimator.EVENT_BUS.post(new AnimatorEvent.Reset(this.uuid, this.animationLocation, this.animation));

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
