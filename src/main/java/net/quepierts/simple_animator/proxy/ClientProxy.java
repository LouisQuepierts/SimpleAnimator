package net.quepierts.simple_animator.proxy;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.quepierts.simple_animator.animation.ModelBone;
import net.quepierts.simple_animator.client.ClientAnimator;
import net.quepierts.simple_animator.client.ClientAnimatorManager;
import net.quepierts.simple_animator.client.ClientPlayerNavigator;
import net.quepierts.simple_animator.client.ClientInteractionHandler;
import net.quepierts.simple_animator.network.ModNetwork;
import net.quepierts.simple_animator.network.packet.StopPacket;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {
    public final ClientPlayerNavigator navigator;
    private final ClientInteractionHandler handler;

    public ClientProxy() {
        navigator = new ClientPlayerNavigator();
        handler = new ClientInteractionHandler(this);
    }

    @Override
    public void setup(IEventBus bus) {
        super.setup(bus);
        MinecraftForge.EVENT_BUS.register(new ForgeHandler());
        this.animatorManager = new ClientAnimatorManager();
    }

    /*public void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(animationManager);
    }*/

    @Override
    public boolean isClient() {
        return true;
    }

    public ClientAnimatorManager getAnimatorManager() {
        return (ClientAnimatorManager) this.animatorManager;
    }

    public ClientInteractionHandler getInteractionHandler() {
        return this.handler;
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
                navigator.tick(player);
            }
        }

        @SubscribeEvent
        public void onMovementInputUpdate(MovementInputUpdateEvent event) {
            LocalPlayer player = Minecraft.getInstance().player;
            ClientAnimator animator = getAnimatorManager().getAnimator(player.getUUID());
            Input input = event.getInput();
            if (animator.isRunning() && !animator.getAnimation().isMovable()) {
                if (animator.canStop() && animator.getAnimation().isAbortable() &&
                        (input.forwardImpulse != 0 || input.leftImpulse != 0 || input.jumping || input.shiftKeyDown)) {
                    animator.stop();
                    ModNetwork.sendToServer(new StopPacket(player.getUUID()));
                }

                input.forwardImpulse = 0.0f;
                input.leftImpulse = 0.0f;
                input.jumping = false;
                input.shiftKeyDown = false;
            }
        }

        @SubscribeEvent
        public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
            if (Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON)
                return;

            ClientAnimator animator = getAnimatorManager().getLocalAnimator();

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
