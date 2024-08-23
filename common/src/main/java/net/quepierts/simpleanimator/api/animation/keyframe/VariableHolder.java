package net.quepierts.simpleanimator.api.animation.keyframe;

import com.google.gson.JsonArray;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class VariableHolder {
    private static final byte FLOAT1 = 0;
    private static final byte FLOAT2 = 1;
    private static final byte FLOAT3 = 2;

    public static VariableHolder get(Object o) {
        return new VariableHolder(0.0f);
    }

    public static VariableHolder decode(FriendlyByteBuf byteBuf) {
        byte b = byteBuf.readByte();

        return switch (b) {
            case FLOAT1 -> new VariableHolder(byteBuf);
            case FLOAT2 -> new Float2(byteBuf);
            case FLOAT3 -> new Float3(byteBuf);
            default -> Immutable.INSTANCE;
        };
    }

    public static VariableHolder fromJsonArray(JsonArray array, int size) {
        float x = array.get(0).getAsFloat();
        float y = array.get(1).getAsFloat();
        float z = array.get(2).getAsFloat();
        return switch (size) {
            case 1 -> new VariableHolder(x);
            case 2 -> new VariableHolder.Float2(x, y);
            case 3 -> new VariableHolder.Float3(x, y, z);
            default -> Immutable.INSTANCE;
        };
    }

    private float value;
    protected VariableHolder(FriendlyByteBuf byteBuf) {
        this.value = byteBuf.readFloat();
    }

    public VariableHolder(float value) {
        this.value = value;
    }

    public void toNetwork(FriendlyByteBuf byteBuf) {
        byteBuf.writeByte(FLOAT1);
        byteBuf.writeFloat(this.value);
    }

    public void setValue(float value) {
        this.value = value;
    }

    public void setValue(VariableHolder holder) {
        this.value = holder.value;
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

    public Vector2f getAsVector2f() {
        return new Vector2f(this.value, 0);
    }

    public Vector3f getAsVector3f() {
        return new Vector3f(this.value, 0,  0);
    }

    public float get() {
        return this.value;
    }

    public VariableHolder linerInterpolation(VariableHolder p1, VariableHolder p2, float delta) {
        return new VariableHolder(Interpolation.linerInterpolation(p1.value, p2.value, delta));
    }

    public VariableHolder catmullRomInterpolation(VariableHolder p0, VariableHolder p1, VariableHolder p2, VariableHolder p3, float delta) {
        return new VariableHolder(Interpolation.catmullRomInterpolation(p0.value, p1.value, p2.value, p3.value, delta));
    }

    public static final class Float2 extends VariableHolder {
        private float y;
        private Float2(FriendlyByteBuf byteBuf) {
            super(byteBuf);
            this.y = byteBuf.readFloat();
        }

        public Float2(float x, float y) {
            super(x);
            this.y = y;
        }

        public Float2(Vector2f vector2f) {
            super(vector2f.x);
            this.y = vector2f.y;
        }

        @Override
        public void setValue(VariableHolder holder) {
            Vector2f vector2f = holder.getAsVector2f();
            this.setValue(vector2f.x);
            this.y = vector2f.y;
        }

        @Override
        public Vector2f getAsVector2f() {
            return new Vector2f(get(), y);
        }

        @Override
        public Vector3f getAsVector3f() {
            return new Vector3f(get(), y, 0);
        }

        @Override
        public VariableHolder linerInterpolation(VariableHolder p1, VariableHolder p2, float delta) {
            return new Float2(Interpolation.linerInterpolation(
                    p1.getAsVector2f(),
                    p2.getAsVector2f(),
                    delta)
            );
        }

        @Override
        public VariableHolder catmullRomInterpolation(VariableHolder p0, VariableHolder p1, VariableHolder p2, VariableHolder p3, float delta) {
            return new Float2(Interpolation.catmullRomInterpolation(
                    p0.getAsVector2f(),
                    p1.getAsVector2f(),
                    p2.getAsVector2f(),
                    p3.getAsVector2f(),
                    delta
            ));
        }

        @Override
        public void toNetwork(FriendlyByteBuf byteBuf) {
            byteBuf.writeByte(FLOAT2);
            byteBuf.writeFloat(get());
            byteBuf.writeFloat(y);
        }
    }

    public static final class Float3 extends VariableHolder {
        private float y;
        private float z;

        private Float3(FriendlyByteBuf byteBuf) {
            super(byteBuf);
        }

        public Float3(float x, float y, float z) {
            super(x);
            this.y = y;
            this.z = z;
        }

        public Float3(Vector3f vector3f) {
            super(vector3f.x);
            this.y = vector3f.y;
            this.z = vector3f.z;
        }

        @Override
        public void setValue(VariableHolder holder) {
            Vector3f vector3f = holder.getAsVector3f();
            this.setValue(vector3f.x);
            this.y = vector3f.y;
            this.z = vector3f.z;
        }

        @Override
        public Vector2f getAsVector2f() {
            return new Vector2f(get(), y);
        }

        @Override
        public Vector3f getAsVector3f() {
            return new Vector3f(get(), y, z);
        }

        @Override
        public VariableHolder linerInterpolation(VariableHolder p1, VariableHolder p2, float delta) {
            return new Float3(Interpolation.linerInterpolation(
                    p1.getAsVector3f(),
                    p2.getAsVector3f(),
                    delta
            ));
        }

        @Override
        public VariableHolder catmullRomInterpolation(VariableHolder p0, VariableHolder p1, VariableHolder p2, VariableHolder p3, float delta) {
            return new Float3(Interpolation.catmullRomInterpolation(
                    p0.getAsVector3f(),
                    p1.getAsVector3f(),
                    p2.getAsVector3f(),
                    p3.getAsVector3f(),
                    delta
            ));
        }
    }

    public static final class Immutable extends VariableHolder {
        public static final Immutable INSTANCE = new Immutable(0.0f);
        public Immutable(float value) {
            super(value);
        }

        @Override
        public void setValue(float value) {}
    }
}
