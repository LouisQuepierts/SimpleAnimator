package net.quepierts.simpleanimator.api.animation.keyframe;

import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simpleanimator.api.animation.LerpMode;
import org.joml.Quaternionf;

public class QuaternionKeyFrame extends KeyFrame<Quaternionf> {
   public QuaternionKeyFrame(FriendlyByteBuf byteBuf) {
       super(byteBuf, QuaternionKeyFrame::readValue);
   }

    private static Quaternionf readValue(FriendlyByteBuf byteBuf) {
        return byteBuf.readQuaternion();
    }

    public QuaternionKeyFrame(float time, Quaternionf obj, LerpMode mode) {
        super(time, obj, mode);
    }

    @Override
    public void toNetwork(FriendlyByteBuf byteBuf) {
        super.toNetwork(byteBuf);
        byteBuf.writeQuaternion(this.value);
    }

    @Override
    public Quaternionf linerInterpolation(Quaternionf p1, Quaternionf p2, float delta) {
        return Interpolation.linerInterpolation(p1, p2, delta);
    }

    @Override
    public Quaternionf catmullRomInterpolation(Quaternionf p0, Quaternionf p1, Quaternionf p2, Quaternionf p3, float delta) {
        return Interpolation.catmullRomInterpolation(p0, p1, p2, p3, delta);
    }
}
