package net.quepierts.simpleanimator.api.animation.keyframe;

import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simpleanimator.api.animation.LerpMode;
import org.joml.Vector3f;

public class VectorKeyFrame extends KeyFrame<Vector3f> {
    public VectorKeyFrame(FriendlyByteBuf byteBuf) {
        super(byteBuf, VectorKeyFrame::readValue);
    }

    private static Vector3f readValue(FriendlyByteBuf byteBuf) {
        return byteBuf.readVector3f();
    }

    public VectorKeyFrame(float time, Vector3f obj, LerpMode mode) {
        super(time, obj, mode);
    }

    @Override
    public void toNetwork(FriendlyByteBuf byteBuf) {
        super.toNetwork(byteBuf);
        byteBuf.writeVector3f(this.value);
    }

    @Override
    public Vector3f linerInterpolation(Vector3f p1, Vector3f p2, float delta) {
        return Interpolation.linerInterpolation(p1, p2, delta);
    }

    @Override
    public Vector3f catmullRomInterpolation(Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, float delta) {
        return Interpolation.catmullRomInterpolation(p0, p1, p2, p3, delta);
    }
}
