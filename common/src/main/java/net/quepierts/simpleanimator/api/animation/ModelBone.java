package net.quepierts.simpleanimator.api.animation;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

public enum ModelBone {
    ROOT((byte) 0, null),
    BODY((byte) 1, null),
    HEAD((byte) 2, IKBone.HEAD),
    LEFT_ARM((byte) 4, IKBone.LEFT_ARM),
    RIGHT_ARM((byte) 8, IKBone.RIGHT_ARM),
    LEFT_LEG((byte) 16, null),
    RIGHT_LEG((byte) 32, null);

    private final byte mask;
    @Nullable
    private final IKBone ik;

    private static final ImmutableMap<String, ModelBone> REF;

    ModelBone(byte mask, IKBone ik) {
        this.mask = mask;
        this.ik = ik;
    }

    public static ModelBone fromString(String string) {
        return REF.get(string.toLowerCase());
    }

    public byte write(byte flag) {
        return (byte) (flag | this.mask);
    }

    public byte remove(byte flag) {
        return (byte) (flag ^ this.mask);
    }

    public boolean in(int flag) {
        return (flag & this.mask) != 0;
    }

    public IKBone getIk() {
        return ik;
    }

    static {
        ImmutableMap.Builder<String, ModelBone> builder = new ImmutableMap.Builder<>();
        for (ModelBone value : values())
            builder.put(value.name().toLowerCase(), value);
        REF = builder.build();
    }
}
