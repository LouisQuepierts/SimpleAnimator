package net.quepierts.simpleanimator.api.animation.keyframe;

import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simpleanimator.api.animation.LerpMode;

public class VariableKeyFrame extends KeyFrame<VariableHolder> {
    public VariableKeyFrame(FriendlyByteBuf byteBuf) {
        super(byteBuf, VariableHolder::new);
    }

    public VariableKeyFrame(float time, VariableHolder obj, LerpMode mode) {
        super(time, obj, mode);
    }

    @Override
    public void toNetwork(FriendlyByteBuf byteBuf) {
        super.toNetwork(byteBuf);
        this.value.toNetwork(byteBuf);
    }

    @Override
    public VariableHolder linerInterpolation(VariableHolder p1, VariableHolder p2, float delta) {
        return Interpolation.linerInterpolation(p1, p2, delta);
    }

    @Override
    public VariableHolder catmullRomInterpolation(VariableHolder p0, VariableHolder p1, VariableHolder p2, VariableHolder p3, float delta) {
        return Interpolation.catmullRomInterpolation(p0, p1, p2, p3, delta);
    }
}
