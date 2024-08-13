package net.quepierts.simpleanimator.core.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.api.IAnimateHandler;
import net.quepierts.simpleanimator.api.event.client.ClientNavigatorEvent;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;

@Environment(EnvType.CLIENT)
public class ClientPlayerNavigator {
    private Player target = null;
    private float forward = 0.0f;
    private float left = 0.0f;

    private Vec3 targetPosition = null;
    private Vec3 lastTargetPosition = Vec3.ZERO;

    private Phrase phrase = Phrase.IDLE;
    private long timer = 0;
    private boolean navigating = false;
    private Runnable post;

    public void tick() {
        switch (phrase) {
            case RUNNING:
                LocalPlayer player = Minecraft.getInstance().player;

                if (!lastTargetPosition.equals(target.position())) {
                    lastTargetPosition = target.position();
                    targetPosition = PlayerUtils.getRelativePositionWorldSpace(target, forward, left);
                }

                if (timer ++ > 1000) {
                    this.stop(false);
                    return;
                }

                if (((IAnimateHandler) player).simpleanimator$getAnimator().isRunning()) {
                    return;
                }

                if (player.distanceToSqr(targetPosition) < 0.001) {
                    player.moveTo(targetPosition);
                    this.phrase = Phrase.FINISH;
                    this.timer = 0;
                    return;
                }

                Vec3 subtract = targetPosition.subtract(player.position()).multiply(1, 0, 1);
                Vec3 direction = subtract.normalize().scale(player.getSpeed());
                Vec3 vec3 = subtract.lengthSqr() < direction.lengthSqr() ? subtract : direction;

                if (!PlayerUtils.canPositionStand(player.position().add(vec3), player.level(), 0.5f)) {
                    player.setDeltaMovement(0, 0, 0);
                    this.stop(false);
                    return;
                }

                player.addDeltaMovement(vec3);

                player.lookAt(EntityAnchorArgument.Anchor.EYES, lastTargetPosition.add(0, target.getEyeHeight(), 0));
                break;
            case FINISH:
                if (timer ++ > 20) {
                    timer = 0;

                    SimpleAnimator.LOGGER.info("Finish");
                    if (this.post != null)
                        post.run();

                    this.stop(true);
                }
                break;
        }

    }

    public void navigateTo(Player player, float forward, float left, Runnable post) {
        LocalPlayer local = Minecraft.getInstance().player;

        if (SimpleAnimator.EVENT_BUS.post(new ClientNavigatorEvent.Start(player, forward, left)).isCanceled())
            return;

        if (PlayerUtils.isRiding(local) && !player.onGround() && !PlayerUtils.inSameDimension(local, player))
            return;

        this.phrase = Phrase.RUNNING;
        this.target = player;
        this.forward = forward;
        this.left = left;
        this.post = post;
        this.navigating = true;
        this.timer = 0;
    }

    public boolean isNavigating() {
        return navigating;
    }

    public void stop(boolean finished) {
        SimpleAnimator.EVENT_BUS.post(new ClientNavigatorEvent.End(finished));
        this.navigating = false;
        this.target = null;
        this.targetPosition = null;
        this.lastTargetPosition = Vec3.ZERO;
        this.post = null;
        this.timer = 0;
        this.phrase = Phrase.IDLE;
    }

    private enum Phrase {
        IDLE,
        RUNNING,
        FINISH
    }
}
