package net.quepierts.simpleanimator.fabric.proxy;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;

public class FabricClientProxy {
    public static void setup() {
        WorldRenderEvents.BEFORE_ENTITIES.register(FabricClientProxy::onWorldRenderBeforeEntities);
        ClientTickEvents.START_CLIENT_TICK.register(FabricClientProxy::onClientTick);
        proxy = SimpleAnimator.getClient();
    }

    private static ClientProxy proxy;
    private static boolean canClear = false;
    private static long time = Util.getMillis();

    private static void onWorldRenderBeforeEntities(WorldRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        long t = Util.getMillis();

        if (!minecraft.isPaused() && minecraft.level != null) {
            canClear = true;
            proxy.getAnimatorManager().tick((t - time) / 1000f);
        }

        time = t;
    }

    private static void onClientTick(Minecraft minecraft) {
        if (minecraft.level != null) {
            if (proxy.getNavigator().isNavigating()) {
                proxy.getNavigator().tick();
            }
        } else if (canClear) {
            proxy.getAnimatorManager().clear();
            canClear = false;
        }
    }

}
