package net.quepierts.simpleanimator.core.client.state;

import net.quepierts.simpleanimator.api.animation.AnimationState;
import net.quepierts.simpleanimator.core.client.ClientAnimator;

public class StateEnter implements IAnimationState {
    @Override
    public AnimationState getNext(ClientAnimator animator) {
        return AnimationState.LOOP;
    }
}
