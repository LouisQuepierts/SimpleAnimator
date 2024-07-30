package net.quepierts.simple_animator.core.client.state;

import net.quepierts.simple_animator.core.animation.AnimationState;
import net.quepierts.simple_animator.core.client.ClientAnimator;

public class StateExit implements IAnimationState {
    @Override
    public AnimationState getNext(ClientAnimator animator) {
        return AnimationState.IDLE;
    }
}
