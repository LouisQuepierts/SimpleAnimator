package net.quepierts.simpleanimator.core;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PlayerUtils {
    public static boolean isRiding(Entity player) {
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

    public static Vec3 getRelativePositionWorldSpace(Player player, double forward, double left) {
        Vec2 vec2 = new Vec2(0, player.yBodyRot);
        Vec3 vec3 = player.position();
        final float f = Mth.cos((vec2.y + 90.0F) * ((float)Math.PI / 180F));
        final float f1 = Mth.sin((vec2.y + 90.0F) * ((float)Math.PI / 180F));
        final double d0 = f * forward - f1 * left;
        final double d2 = f1 * forward + f * left;
        return new Vec3(vec3.x + d0, vec3.y, vec3.z + d2);
    }

    public static Vec3 getRelativePosition(Player player, double forward, double left) {
        Vec2 vec2 = new Vec2(0, player.yBodyRot);
        final float f = Mth.cos((vec2.y + 90.0F) * ((float)Math.PI / 180F));
        final float f1 = Mth.sin((vec2.y + 90.0F) * ((float)Math.PI / 180F));
        return new Vec3(f * forward - f1 * left, 0, f1 * forward + f * left);
    }

    public static double distanceSqr2D(Vec3 src, Vec3 dest) {
        double x = dest.x - src.x;
        double z = dest.z - src.z;
        return x * x + z * z;
    }

    public static boolean inSameDimension(Player a, Player b) {
        return a.level() == b.level();
    }
}
