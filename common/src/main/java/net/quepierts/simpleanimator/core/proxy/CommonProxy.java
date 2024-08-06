package net.quepierts.simpleanimator.core.proxy;

import net.quepierts.simpleanimator.api.animation.Animator;
import net.quepierts.simpleanimator.core.animation.AnimationManager;
import net.quepierts.simpleanimator.core.animation.AnimatorManager;
import net.quepierts.simpleanimator.core.animation.InteractionManager;
import net.quepierts.simpleanimator.core.config.CommonConfiguration;

public class CommonProxy {
    protected final AnimatorManager<? extends Animator> animatorManager;
    protected final AnimationManager animationManager;
    protected final InteractionManager interactionManager;

    private final Runnable setup;
    private final CommonConfiguration config;
    
    public CommonProxy(Runnable setup) {
        this(new AnimatorManager<>(), setup);
    }
    
    protected CommonProxy(AnimatorManager<? extends Animator> animatorManager, Runnable setup) {
        this.animationManager = new AnimationManager();
        this.animatorManager = animatorManager;
        this.interactionManager = new InteractionManager();
        this.setup = setup;
        this.config = CommonConfiguration.load();
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

    public CommonConfiguration getConfig() {
        return config;
    }

    public void setup() {
        this.setup.run();
    }
}
