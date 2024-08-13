package net.quepierts.simpleanimator.api.animation;

import com.google.common.collect.ImmutableMap;

public enum ModelBone {
    ROOT((byte) 0),
    BODY((byte) 1),
    HEAD((byte) 2),
    LEFT_ARM((byte) 4),
    RIGHT_ARM((byte) 8),
    LEFT_LEG((byte) 16),
    RIGHT_LEG((byte) 32);

    private final byte mask;

    private static final ImmutableMap<String, ModelBone> REF;

    ModelBone(byte mask) {
        this.mask = mask;
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

    static {
        ImmutableMap.Builder<String, ModelBone> builder = new ImmutableMap.Builder<>();
        for (ModelBone value : values())
            builder.put(value.name().toLowerCase(), value);
        REF = builder.build();
    }
}
