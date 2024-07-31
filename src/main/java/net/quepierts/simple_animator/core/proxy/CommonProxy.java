package net.quepierts.simple_animator.core.proxy;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.quepierts.simple_animator.core.common.animation.AnimationManager;
import net.quepierts.simple_animator.core.common.animation.Animator;
import net.quepierts.simple_animator.core.common.animation.InteractionManager;
import net.quepierts.simple_animator.core.common.animation.AnimatorManager;
import net.quepierts.simple_animator.core.common.command.AnimateCommand;
import net.quepierts.simple_animator.core.common.command.InteractCommand;
import net.quepierts.simple_animator.core.network.ModNetwork;
import org.slf4j.Logger;

import java.util.UUID;

public class CommonProxy {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected AnimatorManager<? extends Animator> animatorManager;
    protected AnimationManager animationManager;
    protected InteractionManager interactionManager;

    public CommonProxy() {
        this.animatorManager = new AnimatorManager<>();
        this.animationManager = new AnimationManager();
        this.interactionManager = new InteractionManager();
    }

    public void setup(IEventBus bus) {
        MinecraftForge.EVENT_BUS.register(new ForgeHandler());
        bus.addListener(CommonProxy::commonSetup);

    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Setup Mod Network");
        ModNetwork.register();
    }

    public boolean isClient() {
        return false;
    }

    public AnimatorManager<? extends Animator> getAnimatorManager() {
        return animatorManager;
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public InteractionManager getInteractionManager() {
        return interactionManager;
    }

    private class ForgeHandler {
        @SubscribeEvent
        public void onCommandRegister(RegisterCommandsEvent event) {
            AnimateCommand.register(event.getDispatcher());
            InteractCommand.register(event.getDispatcher());
        }

        @SubscribeEvent
        public void onReload(AddReloadListenerEvent event) {
            animatorManager.clear();
            /*if (!isClient()) {
                LOGGER.info("Server Animation Manager");
                event.addListener(animationManager);
            }*/
            event.addListener(animationManager);
        }

        @SubscribeEvent
        public void onEntityJoinLevel(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof Player) {
                UUID uuid = event.getEntity().getUUID();
                if (!animatorManager.exist(uuid)) {
                    animatorManager.get(uuid);
                }

                if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                    animatorManager.sync(serverPlayer);
                }
            }
        }

        @SubscribeEvent
        public void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
            UUID uuid = event.getEntity().getUUID();
            animatorManager.remove(uuid);
        }

        @SubscribeEvent
        public void onOnDatapackSync(OnDatapackSyncEvent event) {
            if (event.getPlayer() == null)
                return;

            animationManager.sync(event.getPlayer());
        }
    }
}
