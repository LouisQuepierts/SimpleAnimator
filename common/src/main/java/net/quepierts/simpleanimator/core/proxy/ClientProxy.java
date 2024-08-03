package net.quepierts.simpleanimator.core.proxy;

import net.quepierts.simpleanimator.core.client.ClientAnimatorManager;
import net.quepierts.simpleanimator.core.client.ClientInteractionManager;
import net.quepierts.simpleanimator.core.client.ClientPlayerNavigator;

public class ClientProxy extends CommonProxy {
    private final ClientPlayerNavigator navigator;
    private final Runnable setup;

    public ClientProxy(Runnable common, Runnable client) {
        super(new ClientAnimatorManager(), new ClientInteractionManager(), common);
        this.navigator = new ClientPlayerNavigator();
        this.setup = client;
    }
    
    public ClientPlayerNavigator getNavigator() {
        return navigator;
    }

    public ClientAnimatorManager getClientAnimatorManager() {
        return (ClientAnimatorManager) getAnimatorManager();
    }

    public ClientInteractionManager getClientInteractionHandler() {
        return (ClientInteractionManager) getInteractionManager();
    }

    @Override
    public void setup() {
        super.setup();
        this.setup.run();
    }
}
