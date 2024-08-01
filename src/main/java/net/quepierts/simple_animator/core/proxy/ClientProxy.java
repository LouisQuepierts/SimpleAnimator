package net.quepierts.simple_animator.core.proxy;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.quepierts.simple_animator.core.client.ClientAnimator;
import net.quepierts.simple_animator.core.client.ClientAnimatorManager;
import net.quepierts.simple_animator.core.client.ClientInteractionManager;
import net.quepierts.simple_animator.core.client.ClientPlayerNavigator;
import net.quepierts.simple_animator.core.common.animation.ModelBone;
import net.quepierts.simple_animator.core.network.ModNetwork;
import net.quepierts.simple_animator.core.network.packet.AnimatorStopPacket;
import net.quepierts.simple_animator.core.network.packet.InteractCancelPacket;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    public final ClientPlayerNavigator navigator;

    public ClientProxy() {
        this.navigator = new ClientPlayerNavigator();
        this.interactionManager = new ClientInteractionManager(this);
        this.animatorManager = new ClientAnimatorManager();
    }

    @Override
    public void setup(IEventBus bus) {
        super.setup(bus);
        MinecraftForge.EVENT_BUS.register(new ForgeHandler());
    }

    /*public void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(animationManager);
    }*/

    @Override
    public boolean isClient() {
        return true;
    }

    public ClientAnimatorManager getClientAnimatorManager() {
        return (ClientAnimatorManager) this.animatorManager;
    }

    public ClientInteractionManager getClientInteractionHandler() {
        return (ClientInteractionManager) this.interactionManager;
    }

    public ClientPlayerNavigator getNavigator() {
        return navigator;
    }

    private class ForgeHandler {
        private boolean canClear = false;

        @SubscribeEvent
        public void onRenderTick(TickEvent.RenderTickEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.isPaused() && minecraft.level != null) {
                canClear = true;
                animatorManager.tick(event.renderTickTime);
            }
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            Player player = Minecraft.getInstance().player;

            if (canClear && (Minecraft.getInstance().level == null || player == null)) {
                animatorManager.clear();
                canClear = false;
            }
        }

        @SubscribeEvent
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (navigator.isNavigating() && event.phase == TickEvent.Phase.END && event.player instanceof LocalPlayer player) {
                navigator.tick();
            }
        }

        @SubscribeEvent
        public void onMovementInputUpdate(MovementInputUpdateEvent event) {
            LocalPlayer player = Minecraft.getInstance().player;
            Input input = event.getInput();
            boolean hasInput = input.forwardImpulse != 0 || input.leftImpulse != 0 || input.jumping || input.shiftKeyDown;

            if (hasInput) {
                UUID uuid = player.getUUID();
                if (getNavigator().isNavigating()) {
                    getNavigator().stop();
                }

                if (getClientInteractionHandler().requesting()) {
                    getClientInteractionHandler().cancel(uuid);
                    ModNetwork.sendToServer(new InteractCancelPacket(uuid));
                    return;
                }

                ClientAnimator animator = getClientAnimatorManager().getLocalAnimator();

                if (animator.isRunning() && !animator.getAnimation().isMovable()) {
                    if (animator.canStop() && animator.getAnimation().isAbortable()) {
                        animator.stop();
                        ModNetwork.sendToServer(new AnimatorStopPacket(uuid));
                    }

                    input.forwardImpulse = 0.0f;
                    input.leftImpulse = 0.0f;
                    input.jumping = false;
                    input.shiftKeyDown = false;
                }
            }
        }

        @SubscribeEvent
        public void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
            ClientAnimator animator = getClientAnimatorManager().getLocalAnimator();

            if (animator.isRunning() && animator.getAnimation().isOverride()) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }

        @SubscribeEvent
        public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON)
                return;

            ClientAnimator animator = getClientAnimatorManager().getLocalAnimator();

            if (animator.isRunning() && animator.getAnimation().isOverride() && animator.isProcessed()) {
                ClientAnimator.Cache root = animator.getCache(ModelBone.ROOT);
                ClientAnimator.Cache head = animator.getCache(ModelBone.HEAD);

                LocalPlayer player = Minecraft.getInstance().player;
                assert player != null;
                float yRot = player.yHeadRot - player.yBodyRot;
                float xRot = player.getXRot();

                float x = (float) Math.toDegrees(root.rotation().x + head.rotation().x);
                float y = (float) Math.toDegrees(root.rotation().y + head.rotation().y);
                event.setPitch(event.getPitch() + x - xRot);
                event.setYaw(event.getYaw() + y - yRot);
                event.setRoll(event.getRoll() + (float) Math.toDegrees(root.rotation().z + head.rotation().z));
            }
        }
    }
}
