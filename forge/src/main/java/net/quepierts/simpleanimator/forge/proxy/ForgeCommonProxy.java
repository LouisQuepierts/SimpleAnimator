package net.quepierts.simpleanimator.forge.proxy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;

import java.util.UUID;

public class ForgeCommonProxy {
    public static void setup() {
        MinecraftForge.EVENT_BUS.register(new ForgeCommonProxy());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ForgeCommonProxy::commonSetup);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        NetworkPackets.register();
    }

    private final CommonProxy proxy;

    private ForgeCommonProxy() {
        this.proxy = SimpleAnimator.getProxy();
    }

    @SubscribeEvent
    public void onReload(AddReloadListenerEvent event) {
        this.proxy.getAnimatorManager().clear();
        this.proxy.getInteractionManager().clear();
            /*if (!isClient()) {
                LOGGER.info("Server Animation Manager");
                event.addListener(animationManager);
            }*/
        event.addListener(this.proxy.getAnimationManager());
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            this.proxy.getAnimatorManager().sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        UUID uuid = event.getEntity().getUUID();
        this.proxy.getAnimatorManager().remove(uuid);
    }

    @SubscribeEvent
    public void onOnDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null)
            return;

        this.proxy.getAnimatorManager().sync(event.getPlayer());
    }
}
