package net.quepierts.simple_animator.core.client;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.common.PlayerUtils;

@OnlyIn(Dist.CLIENT)
public class ClientPlayerNavigator {
    private Vec3 targetPosition = null;
    private long timer = 0;
    private long duration = 0;
    private boolean navigating = false;

    public void tick(LocalPlayer player) {
        if (PlayerUtils.distanceSqr2D(player.position(), targetPosition) < 0.0001 || timer++ > duration) {
            stopNavigating(player);
            return;
        }

        Vec3 direction = targetPosition.subtract(player.position()).normalize();
        double speed = 0.1; // 控制移动速度
        player.setDeltaMovement(direction.scale(speed));

        /*double dX = targetPosition.x - player.getX();
        double dZ = targetPosition.z - player.getZ();
        float targetYaw = (float) (Mth.atan2(dZ, dX) * (180 / Math.PI)) - 90;
        player.setYRot(targetYaw);*/
    }

    public void navigateTo(Vec3 position) {
        SimpleAnimator.LOGGER.info("Position: {}", position);
        targetPosition = position;
        navigating = true;
        timer = 0;
        duration = 1000;
    }

    private void stopNavigating(LocalPlayer player) {
        navigating = false;
        player.setPos(targetPosition);
        player.setDeltaMovement(Vec3.ZERO); // 停止移动
        targetPosition = null;
    }

    public boolean isNavigating() {
        return navigating;
    }
}
