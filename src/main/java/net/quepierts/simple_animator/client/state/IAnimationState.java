package net.quepierts.simple_animator.client.state;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.quepierts.simple_animator.animation.AnimationState;
import net.quepierts.simple_animator.client.ClientAnimator;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public interface IAnimationState {
    default void enter(ClientAnimator animator) {

    }

    default boolean shouldEnd(ClientAnimator animator) {
        return true;
    }

    default void exit(ClientAnimator animator) {}

    AnimationState getNext(ClientAnimator animator);

    default Vector3f getDest(Vector3f keyframe, Vector3f vector3f) {
        return keyframe;
    }

    default Vector3f getSrc(Vector3f cache, Vector3f vector3f) {
        return cache;
    }

    class Impl {
        static final IAnimationState[] IMPL;

        @NotNull
        public static IAnimationState get(@NotNull AnimationState state) {
            return IMPL[state.ordinal()];
        }

        static {
            IMPL = new IAnimationState[]{
                    new StateIdle(),
                    new StateRequest(),
                    new StateWait(),
                    new StateEnter(),
                    new StateLoop(),
                    new StateExit()
            };
        }
    }
}
