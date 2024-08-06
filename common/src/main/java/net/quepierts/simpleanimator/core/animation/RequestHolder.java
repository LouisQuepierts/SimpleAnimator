package net.quepierts.simpleanimator.core.animation;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RequestHolder {
    @Nullable private UUID target;
    @Nullable private ResourceLocation interaction;

    public RequestHolder(UUID uuid) {
    }

    public boolean hasRequest() {
        return this.target != null;
    }

    public @Nullable UUID getTarget() {
        return this.target;
    }

    public @Nullable ResourceLocation getInteraction() {
        return this.interaction;
    }

    public void reset() {
        this.target = null;
        this.interaction = null;
    }

    public void set(UUID target, ResourceLocation interaction) {
        this.target = target;
        this.interaction = interaction;
    }
}
