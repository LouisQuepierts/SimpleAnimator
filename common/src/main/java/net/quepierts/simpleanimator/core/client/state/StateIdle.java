package net.quepierts.simpleanimator.core.client.state;

import net.quepierts.simpleanimator.core.animation.AnimationState;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.joml.Vector3f;

public class StateIdle implements IAnimationState {
    @Override
    public void enter(ClientAnimator animator) {
        animator.reset();
    }

    @Override
    public AnimationState getNext(ClientAnimator animator) {
        return animator.getAnimation().hasEnterAnimation() ? AnimationState.ENTER : AnimationState.LOOP;
    }

    @Override
    public Vector3f getDest(Vector3f keyframe, Vector3f vector3f) {
        return vector3f;
    }

    @Override
    public Vector3f getSrc(Vector3f cache, Vector3f vector3f) {
        return vector3f;
    }
}
