package net.quepierts.simple_animator.client.state;

import net.quepierts.simple_animator.animation.AnimationState;
import net.quepierts.simple_animator.client.ClientAnimator;

public class StateLoop implements IAnimationState {

    @Override
    public boolean shouldEnd(ClientAnimator animator) {
        return !animator.getAnimation().repeatable() || animator.getNextState() != AnimationState.LOOP;
    }

    @Override
    public AnimationState getNext(ClientAnimator animator) {
        return animator.getAnimation().hasExitAnimation() ? AnimationState.EXIT : AnimationState.IDLE;
    }
}
