package net.quepierts.simpleanimator.forge.proxy;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;

public class ForgeClientProxy {
    public static void setup() {
        MinecraftForge.EVENT_BUS.register(new ForgeClientProxy());
    }

    private final ClientProxy proxy;
    private ForgeClientProxy() {
        this.proxy = SimpleAnimator.getClient();
    }

    private boolean canClear = false;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (event.phase == TickEvent.Phase.END)
            return;

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

    /*@SubscribeEvent
    public void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        ClientAnimator animator = this.proxy.getClientAnimatorManager().getLocalAnimator();

        if (animator.isRunning() && animator.getAnimation().isOverride()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }*/
}
