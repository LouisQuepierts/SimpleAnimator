package net.quepierts.simpleanimator.core.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.core.PlayerUtils;

@Environment(EnvType.CLIENT)
public class ClientPlayerNavigator {
    private Player target = null;
    private float forward = 0.0f;
    private float left = 0.0f;

    private Vec3 targetPosition = null;
    private Vec3 lastTargetPosition = Vec3.ZERO;

    private long timer = 0;
    private boolean navigating = false;
    private Runnable post;


    public void tick() {
        if (!lastTargetPosition.equals(target.position())) {
            lastTargetPosition = target.position();
            targetPosition = PlayerUtils.getRelativePosition(target, forward, left);
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (timer ++ > 1000) {
            this.stop();
            return;
        }

        if (PlayerUtils.distanceSqr2D(player.position(), targetPosition) < 0.001) {
            finish(player);
            return;
        }

        Vec3 subtract = targetPosition.subtract(player.position()).multiply(1, 0, 1);
        Vec3 direction = subtract.normalize().scale(player.getSpeed());
        player.addDeltaMovement(subtract.lengthSqr() < direction.lengthSqr() ? subtract : direction);

        player.lookAt(EntityAnchorArgument.Anchor.EYES, lastTargetPosition.add(0, target.getEyeHeight(), 0));
    }

    public void navigateTo(Player player, float forward, float left, Runnable post) {
        this.target = player;
        this.forward = forward;
        this.left = left;
        this.post = post;
        this.navigating = true;
        this.timer = 0;
    }

    private void finish(LocalPlayer player) {
        player.setDeltaMovement(Vec3.ZERO); // 停止移动

        if (post != null)
            post.run();

        this.stop();
    }

    public boolean isNavigating() {
        return navigating;
    }

    public void stop() {
        this.navigating = false;
        this.target = null;
        this.targetPosition = null;
        this.lastTargetPosition = Vec3.ZERO;
        this.post = null;
        this.timer = 0;
    }
}
