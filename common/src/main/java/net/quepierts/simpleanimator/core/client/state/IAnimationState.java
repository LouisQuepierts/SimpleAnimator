package net.quepierts.simpleanimator.core.client.state;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.quepierts.simpleanimator.api.animation.AnimationState;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public interface IAnimationState {
    default void enter(ClientAnimator animator) {

    }

    default boolean shouldEnd(ClientAnimator animator) {
        return true;
    }

    default void exit(ClientAnimator animator) {}

    AnimationState getNext(ClientAnimator animator);

    default <T> T getDest(T keyframe, T curr) {
        return keyframe;
    }

    default <T> T getSrc(T cache, T curr) {
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
                    new StateEnter(),
                    new StateLoop(),
                    new StateExit()
            };
        }
    }
}
