package net.quepierts.simple_animator.core.common;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PlayerUtils {
    public static boolean isRiding(Player player) {
        return player != null && player.getVehicle() != null;
    }

    public static double normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle >= 180) {
            angle -= 360;
        }
        if (angle < -180) {
            angle += 360;
        }
        return angle;
    }

    public static float normalizeRadians(double rad) {
        return (float) Math.toRadians(normalizeAngle((float) Math.toDegrees(rad)));
    }

    public static Vec3 getRelativePosition(Player player, double forwards, double left) {
        Vec2 vec2 = new Vec2(0, player.getYRot());
        Vec3 vec3 = player.position();
        float f = Mth.cos((vec2.y + 90.0F) * ((float)Math.PI / 180F));
        float f1 = Mth.sin((vec2.y + 90.0F) * ((float)Math.PI / 180F));
        Vec3 vec31 = new Vec3(f, 0, f1);
        Vec3 vec33 = new Vec3(-f1, 0, f);
        double d0 = vec31.x * forwards + vec33.x * left;
        double d2 = vec31.z * forwards + vec33.z * left;
        return new Vec3(vec3.x + d0, vec3.y, vec3.z + d2);
    }

    public static double distanceSqr2D(Vec3 src, Vec3 dest) {
        double x = dest.x - src.x;
        double z = dest.z - src.z;
        return x * x + z * z;
    }
}
