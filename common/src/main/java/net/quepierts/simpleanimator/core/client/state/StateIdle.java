package net.quepierts.simpleanimator.core.client.state;

import net.quepierts.simpleanimator.api.animation.AnimationState;
import net.quepierts.simpleanimator.core.client.ClientAnimator;

public class StateIdle implements IAnimationState {
    @Override
    public void enter(ClientAnimator animator) {
        animator.reset(true);
    }

    @Override
    public AnimationState getNext(ClientAnimator animator) {
        return animator.getAnimation().hasEnterAnimation() ? AnimationState.ENTER : AnimationState.LOOP;
    }

    @Override
    public <T> T getDest(T keyframe, T curr) {
        return curr;
    }

    @Override
    public <T> T getSrc(T cache, T curr) {
        return curr;
    }
}
