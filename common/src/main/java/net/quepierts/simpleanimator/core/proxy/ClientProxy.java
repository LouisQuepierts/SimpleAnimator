package net.quepierts.simpleanimator.core.proxy;

import net.quepierts.simpleanimator.core.client.ClientAnimatorManager;
import net.quepierts.simpleanimator.core.client.ClientPlayerNavigator;
import net.quepierts.simpleanimator.core.config.ClientConfiguration;

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
}
