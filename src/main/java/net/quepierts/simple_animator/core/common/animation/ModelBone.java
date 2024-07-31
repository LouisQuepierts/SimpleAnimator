package net.quepierts.simple_animator.core.common.animation;

import com.google.common.collect.ImmutableMap;

public enum ModelBone {
    ROOT, BODY, HEAD, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG;

    private static final ImmutableMap<String, ModelBone> REF;

    public static ModelBone fromString(String string) {
        return REF.get(string.toLowerCase());
    }

    static {
        ImmutableMap.Builder<String, ModelBone> builder = new ImmutableMap.Builder<>();
        for (ModelBone value : values())
            builder.put(value.name().toLowerCase(), value);
        REF = builder.build();
    }
}
