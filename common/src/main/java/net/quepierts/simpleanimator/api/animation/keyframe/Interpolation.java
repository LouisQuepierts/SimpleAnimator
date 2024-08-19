package net.quepierts.simpleanimator.api.animation.keyframe;

import net.minecraft.util.Mth;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Interpolation {
    public static <T> T interpolation(KeyFrame<T>[] keyframes, T out, float time) {
        KeyFrame<T> kf0 = null, kf1 = null, kf2 = null, kf3 = null;
        for (int i = 0; i < keyframes.length - 1; i++) {
            if (time >= keyframes[i].getTime() && time <= keyframes[i + 1].getTime()) {
                kf1 = keyframes[i];
                kf2 = keyframes[i + 1];
                kf0 = (i > 0) ? keyframes[i - 1] : kf1;
                kf3 = (i + 2 < keyframes.length) ? keyframes[i + 2] : kf2;
                break;
            }
        }

        if (kf1 == null || kf2 == null) {
            return out;
        }

        float localT = (time - kf1.getTime()) / (kf2.getTime() - kf1.getTime());

        switch (kf2.getMode()) {
            case LINEAR -> out = kf2.linerInterpolation(kf1.getValue(), kf2.getValue(), localT);
            case CATMULLROM -> out = kf2.catmullRomInterpolation(kf0.getValue(), kf1.getValue(), kf2.getValue(), kf3.getValue(), localT);
            case STEP -> out = kf1.getValue();
        }

        return out;
    }

    public static VariableHolder linerInterpolation(VariableHolder p1, VariableHolder p2, float delta) {
        return new VariableHolder(p1.get() * (1.0F - delta) + p2.get() * delta);
    }

    public static VariableHolder catmullRomInterpolation(VariableHolder p0, VariableHolder p1, VariableHolder p2, VariableHolder p3, float delta) {
        float t2 = delta * delta;
        float t3 = t2 * delta;
        return new VariableHolder(0.5F * (2.0F * p1.get() + (-p0.get() + p2.get()) * delta + (2.0F * p0.get() - 5.0F * p1.get() + 4.0F * p2.get() - p3.get()) * t2 + (-p0.get() + 3.0F * p1.get() - 3.0F * p2.get() + p3.get()) * t3));
    }

    public static Vector3f linerInterpolation(Vector3f p1, Vector3f p2, float delta) {
        return new Vector3f(p1.x * (1.0F - delta) + p2.x * delta, p1.y * (1.0F - delta) + p2.y * delta, p1.z * (1.0F - delta) + p2.z * delta);
    }

    public static Vector3f catmullRomInterpolation(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, float delta) {
        float t2 = delta * delta;
        float t3 = t2 * delta;
        float x = 0.5F * (2.0F * p1.x + (-p0.x + p2.x) * delta + (2.0F * p0.x - 5.0F * p1.x + 4.0F * p2.x - p3.x) * t2 + (-p0.x + 3.0F * p1.x - 3.0F * p2.x + p3.x) * t3);
        float y = 0.5F * (2.0F * p1.y + (-p0.y + p2.y) * delta + (2.0F * p0.y - 5.0F * p1.y + 4.0F * p2.y - p3.y) * t2 + (-p0.y + 3.0F * p1.y - 3.0F * p2.y + p3.y) * t3);
        float z = 0.5F * (2.0F * p1.z + (-p0.z + p2.z) * delta + (2.0F * p0.z - 5.0F * p1.z + 4.0F * p2.z - p3.z) * t2 + (-p0.z + 3.0F * p1.z - 3.0F * p2.z + p3.z) * t3);
        return new Vector3f(x, y, z);
    }

    public static Quaternionf linerInterpolation(Quaternionf p1, Quaternionf p2, float delta) {

        Quaternionf dest = new Quaternionf();
        return p1.slerp(p2, delta, dest);
    }

    // slerp fast mth?
    public static Quaternionf catmullRomInterpolation(Quaternionf p0, Quaternionf p1, Quaternionf p2, Quaternionf p3, float delta) {
        Quaternionf dest = new Quaternionf();
        float cosom = Math.fma(p1.x(), p2.x(), Math.fma(p1.y(), p2.y(), Math.fma(p1.z(), p2.z(), p1.w() * p2.w())));
        float absCosom = Math.abs(cosom);
        float scale0, scale1;
        if (1.0f - absCosom > 1E-6f) {
            float sinSqr = 1.0f - absCosom * absCosom;
            float sinom = Math.invsqrt(sinSqr);
            float omega = (float) Mth.atan2(sinSqr * sinom, absCosom);
            scale0 = Mth.sin((1.0f - delta) * omega) * sinom;
            scale1 = Mth.sin(delta * omega) * sinom;
        } else {
            scale0 = 1.0f - delta;
            scale1 = delta;
        }
        scale1 = cosom >= 0.0f ? scale1 : -scale1;
        dest.x = Math.fma(scale0, p1.x(), scale1 * p2.x());
        dest.y = Math.fma(scale0, p1.y(), scale1 * p2.y());
        dest.z = Math.fma(scale0, p1.z(), scale1 * p2.z());
        dest.w = Math.fma(scale0, p1.w(), scale1 * p2.w());
        return dest;

    }
}
