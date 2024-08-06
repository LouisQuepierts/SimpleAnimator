package net.quepierts.simpleanimator.fabric.proxy;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;

public class FabricClientProxy {
    public static void setup() {
        ClientTickEvents.START_CLIENT_TICK.register(FabricClientProxy::onClientTick);
        proxy = SimpleAnimator.getClient();
    }

    private static ClientProxy proxy;
    private static boolean canClear = false;

    private static void onClientTick(Minecraft minecraft) {
        if (minecraft.level != null) {
            canClear = true;
            if (proxy.getNavigator().isNavigating()) {
                proxy.getNavigator().tick();
            }
        } else if (canClear) {
            proxy.getAnimatorManager().clear();
            canClear = false;
        }
    }

}
