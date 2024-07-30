package net.quepierts.simple_animator.client.state;

import net.quepierts.simple_animator.animation.AnimationState;
import net.quepierts.simple_animator.client.ClientAnimator;

public class StateExit implements IAnimationState {
    @Override
    public AnimationState getNext(ClientAnimator animator) {
        return AnimationState.IDLE;
    }
}
