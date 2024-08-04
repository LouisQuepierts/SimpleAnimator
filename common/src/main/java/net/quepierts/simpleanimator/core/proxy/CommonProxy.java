package net.quepierts.simpleanimator.core.proxy;

import net.quepierts.simpleanimator.core.animation.AnimationManager;
import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.animation.AnimatorManager;
import net.quepierts.simpleanimator.core.animation.InteractionManager;

public class CommonProxy {
    protected final AnimatorManager<? extends Animator> animatorManager;
    protected final AnimationManager animationManager;
    protected final InteractionManager interactionManager;

    private final Runnable setup;
    
    public CommonProxy(Runnable setup) {
        this(new AnimatorManager<>(), setup);
    }
    
    protected CommonProxy(AnimatorManager<? extends Animator> animatorManager, Runnable setup) {
        this.animationManager = new AnimationManager();
        this.animatorManager = animatorManager;
        this.interactionManager = new InteractionManager();
        this.setup = setup;
    }

    public AnimatorManager<? extends Animator> getAnimatorManager() {
        return animatorManager;
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    public void setup() {
        this.setup.run();
    }
}