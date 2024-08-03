package net.quepierts.simpleanimator.core.client.state;

import net.quepierts.simpleanimator.core.animation.AnimationState;
import net.quepierts.simpleanimator.core.client.ClientAnimator;

public class StateExit implements IAnimationState {
    @Override
    public AnimationState getNext(ClientAnimator animator) {
        return AnimationState.IDLE;
    }
}
