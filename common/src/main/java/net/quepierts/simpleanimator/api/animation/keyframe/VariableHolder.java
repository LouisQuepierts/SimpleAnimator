package net.quepierts.simpleanimator.api.animation.keyframe;

import net.minecraft.network.FriendlyByteBuf;

public class VariableHolder {
    public static final VariableHolder ZERO = new VariableHolder(0.0f);
    public static VariableHolder get(Object o) {
        return new VariableHolder(0.0f);
    }
    private float value;
    public VariableHolder(FriendlyByteBuf byteBuf) {
        this.value = byteBuf.readFloat();
    }

    public VariableHolder(float value) {
        this.value = value;
    }

    public void toNetwork(FriendlyByteBuf byteBuf) {
        byteBuf.writeFloat(this.value);
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getAsInt() {
        return (int) this.value;
    }

    public boolean getAsBoolean() {
        return this.value != 0.0f;
    }

    public float getAsFloat() {
        return this.value;
    }

    public float get() {
        return this.value;
    }

    public static final class Immutable extends VariableHolder {
        public Immutable(float value) {
            super(value);
        }

        @Override
        public void setValue(float value) {}
    }
}
