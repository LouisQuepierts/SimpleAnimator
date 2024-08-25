package net.quepierts.simpleanimator.api.animation.keyframe;

import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simpleanimator.api.animation.LerpMode;

import java.util.IdentityHashMap;
import java.util.function.Function;

public abstract class KeyFrame<T> {
     protected final float time;
    protected final LerpMode mode;
    protected final T pre;
    protected T post;

    public KeyFrame(FriendlyByteBuf byteBuf, Function<FriendlyByteBuf, T> decoder) {
        this.time = byteBuf.readFloat();
        this.mode = byteBuf.readEnum(LerpMode.class);
        this.pre = decoder.apply(byteBuf);
        this.post = decoder.apply(byteBuf);
    }

    public KeyFrame(float time, T pre, T post, LerpMode mode) {
        this.time = time;
        this.pre = pre;
        this.post = post;
        this.mode = mode;
    }

    public float getTime() {
        return time;
    }

    public LerpMode getMode() {
        return mode;
    }

    public T getPre() {
        return pre;
    }

    public T getPost() {
        return post;
    }

    public void toNetwork(FriendlyByteBuf byteBuf) {
        byteBuf.writeFloat(this.time);
        byteBuf.writeEnum(this.mode);
    }

    public abstract T linerInterpolation(T p1, T p2, float delta);

    public abstract T catmullRomInterpolation(T p0, T p1, T p2, T p3, float delta);

    public static class Decoder {
        private static final IdentityHashMap<Class<? extends KeyFrame<?>>, Function<FriendlyByteBuf, KeyFrame<?>>> DECODERS;

        public static <T extends KeyFrame<?>> T decode(FriendlyByteBuf byteBuf, Class<T> clazz) {
            Function<FriendlyByteBuf, KeyFrame<?>> decoder = DECODERS.get(clazz);

            if (decoder == null)
                throw new IllegalArgumentException(clazz + "is not a legal type!");

            return (T) decoder.apply(byteBuf);
        }

        static {
            DECODERS = new IdentityHashMap<>(3);
            DECODERS.put(VectorKeyFrame.class, VectorKeyFrame::new);
            DECODERS.put(QuaternionKeyFrame.class, QuaternionKeyFrame::new);
            DECODERS.put(VariableKeyFrame.class, VariableKeyFrame::new);
        }
    }

}
