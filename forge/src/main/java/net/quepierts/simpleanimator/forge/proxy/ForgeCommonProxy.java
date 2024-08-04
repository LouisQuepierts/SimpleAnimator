package net.quepierts.simpleanimator.forge.proxy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.command.AnimateCommand;
import net.quepierts.simpleanimator.core.command.InteractCommand;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;
import net.quepierts.simpleanimator.forge.config.ForgeCommonConfiguration;

import java.util.UUID;

public class ForgeCommonProxy {
    public static void setup() {
        MinecraftForge.EVENT_BUS.register(new ForgeCommonProxy());
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ForgeCommonProxy::commonSetup);
        ForgeCommonConfiguration.register(ModLoadingContext.get(), bus);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        NetworkPackets.register();
    }

    private final CommonProxy proxy;

    private ForgeCommonProxy() {
        this.proxy = SimpleAnimator.getProxy();
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        AnimateCommand.register(event.getDispatcher());
        InteractCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onReload(AddReloadListenerEvent event) {
        this.proxy.getAnimatorManager().reset();
        this.proxy.getInteractionManager().reset();
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

        this.proxy.getAnimationManager().sync(event.getPlayer());
    }
}
