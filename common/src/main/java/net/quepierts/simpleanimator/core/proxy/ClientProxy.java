package net.quepierts.simpleanimator.core.proxy;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.core.animation.Animator;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.client.ClientAnimatorManager;
import net.quepierts.simpleanimator.core.client.ClientPlayerNavigator;
import net.quepierts.simpleanimator.core.config.ClientConfiguration;
import org.joml.Vector3f;

public class ClientProxy extends CommonProxy {
    private final ClientPlayerNavigator navigator;
    private final Runnable setup;
    private final ClientConfiguration config;

    public ClientProxy(Runnable common, Runnable client) {
        super(new ClientAnimatorManager(), common);
        this.navigator = new ClientPlayerNavigator();
        this.setup = client;
        this.config = ClientConfiguration.load();
    }
    
    public ClientPlayerNavigator getNavigator() {
        return navigator;
    }

    public ClientAnimatorManager getClientAnimatorManager() {
        return (ClientAnimatorManager) getAnimatorManager();
    }

    @Override
    public void setup() {
        super.setup();
        this.setup.run();
    }

    public ClientConfiguration getClientConfiguration() {
        return config;
    }

    public void example(AbstractClientPlayer player) {
        Animator animator = ((IAnimateHandler) player).simpleanimator$getAnimator();
        animator.play(ResourceLocation.fromNamespaceAndPath("id", "anim_name"));

        ClientAnimator clientAnimator = (ClientAnimator) animator;
        Vector3f name = clientAnimator.getVariable("name").getAsVector3f();
    }
}
