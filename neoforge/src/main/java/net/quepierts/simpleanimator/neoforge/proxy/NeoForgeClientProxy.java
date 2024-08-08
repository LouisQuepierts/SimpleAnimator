package net.quepierts.simpleanimator.neoforge.proxy;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;

public class NeoForgeClientProxy {
    public static void setup() {
        NeoForge.EVENT_BUS.register(new NeoForgeClientProxy());
    }

    private final ClientProxy proxy;
    private NeoForgeClientProxy() {
        this.proxy = SimpleAnimator.getClient();
    }

    private boolean canClear = false;

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level != null) {
            canClear = true;
            if (this.proxy.getNavigator().isNavigating()) {
                this.proxy.getNavigator().tick();
            }
        } else if (canClear) {
            this.proxy.getAnimatorManager().clear();
            canClear = false;
        }
    }
}
