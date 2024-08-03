package net.quepierts.simpleanimator.forge.proxy;

import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.ModelBone;
import net.quepierts.simpleanimator.core.client.ClientAnimator;
import net.quepierts.simpleanimator.core.command.AnimateCommand;
import net.quepierts.simpleanimator.core.command.InteractCommand;
import net.quepierts.simpleanimator.core.network.packet.AnimatorStopPacket;
import net.quepierts.simpleanimator.core.network.packet.InteractCancelPacket;
import net.quepierts.simpleanimator.core.proxy.ClientProxy;

import java.util.UUID;

public class ForgeClientProxy {
    public static void setup() {
        MinecraftForge.EVENT_BUS.register(new ForgeClientProxy());
    }

    private static long time = Util.getMillis();
    private final ClientProxy proxy;
    private ForgeClientProxy() {
        this.proxy = SimpleAnimator.getClient();
    }

    private boolean canClear = false;
    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        AnimateCommand.register(event.getDispatcher());
        InteractCommand.register(event.getDispatcher());
    }


    /*@SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        long t = Util.getMillis();
        if (!minecraft.isPaused() && minecraft.level != null) {
            canClear = true;
            this.proxy.getAnimatorManager().tick((t - time) / 1000f);
        }

        time = t;
    }*/

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        long t = Util.getMillis();

        if (minecraft.level != null) {
            if (!minecraft.isPaused()) {
                canClear = true;
                this.proxy.getAnimatorManager().tick((t - time) / 1000f);
            }
            if (this.proxy.getNavigator().isNavigating() && event.phase == TickEvent.Phase.END) {
                this.proxy.getNavigator().tick();
            }
        } else if (canClear) {
            this.proxy.getAnimatorManager().clear();
            canClear = false;
        }
        time = t;
    }

    @SubscribeEvent
    public void onMovementInputUpdate(MovementInputUpdateEvent event) {
        LocalPlayer player = Minecraft.getInstance().player;
        Input input = event.getInput();
        boolean hasInput = input.forwardImpulse != 0 || input.leftImpulse != 0 || input.jumping || input.shiftKeyDown;

        if (hasInput) {
            UUID uuid = player.getUUID();
            if (this.proxy.getNavigator().isNavigating()) {
                this.proxy.getNavigator().stop();
            }

            if (this.proxy.getClientInteractionHandler().requesting()) {
                this.proxy.getClientInteractionHandler().cancel(uuid);
                SimpleAnimator.getNetwork().update(new InteractCancelPacket(uuid));
                return;
            }

            ClientAnimator animator = this.proxy.getClientAnimatorManager().getLocalAnimator();

            if (animator.isRunning() && !animator.getAnimation().isMovable()) {
                if (animator.canStop() && animator.getAnimation().isAbortable()) {
                    animator.stop();
                    SimpleAnimator.getNetwork().update(new AnimatorStopPacket(uuid));
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
        ClientAnimator animator = this.proxy.getClientAnimatorManager().getLocalAnimator();

        if (animator.isRunning() && animator.getAnimation().isOverride()) {
            event.setCanceled(true);
            event.setSwingHand(false);
        }
    }

    @SubscribeEvent
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON)
            return;

        ClientAnimator animator = this.proxy.getClientAnimatorManager().getLocalAnimator();

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
