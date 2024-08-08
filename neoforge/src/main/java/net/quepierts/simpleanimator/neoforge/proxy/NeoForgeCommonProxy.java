package net.quepierts.simpleanimator.neoforge.proxy;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.command.AnimateCommand;
import net.quepierts.simpleanimator.core.command.InteractCommand;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import net.quepierts.simpleanimator.core.proxy.CommonProxy;

import java.util.UUID;

public class NeoForgeCommonProxy {
    public static void setup() {
        NeoForge.EVENT_BUS.register(new NeoForgeCommonProxy());

        IEventBus bus = ModLoadingContext.get().getActiveContainer().getEventBus();
        assert bus != null;
        bus.addListener(NeoForgeCommonProxy::onRegisterPayLoad);
    }

    private static void onRegisterPayLoad(final RegisterPayloadHandlersEvent event) {
        NetworkPackets.register();
    }

    private final CommonProxy proxy;

    private NeoForgeCommonProxy() {
        this.proxy = SimpleAnimator.getProxy();
    }

    @SubscribeEvent
    public void onCommandRegister(final RegisterCommandsEvent event) {
        AnimateCommand.register(event.getDispatcher());
        InteractCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onReload(final AddReloadListenerEvent event) {
        this.proxy.getAnimatorManager().reset();
        this.proxy.getInteractionManager().reset();
            /*if (!isClient()) {
                LOGGER.info("Server Animation Manager");
                event.addListener(animationManager);
            }*/
        event.addListener(this.proxy.getAnimationManager());
    }

    @SubscribeEvent
    public void onEntityJoinLevel(final EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            this.proxy.getAnimatorManager().sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onEntityLeaveLevel(final EntityLeaveLevelEvent event) {
        UUID uuid = event.getEntity().getUUID();
        this.proxy.getAnimatorManager().remove(uuid);
        this.proxy.getInteractionManager().remove(uuid);
    }

    @SubscribeEvent
    public void onOnDatapackSync(final OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            this.proxy.getAnimationManager().sync(event.getPlayer());
        } else {
            this.proxy.getAnimationManager().sync(event.getPlayerList());
        }
    }
}
