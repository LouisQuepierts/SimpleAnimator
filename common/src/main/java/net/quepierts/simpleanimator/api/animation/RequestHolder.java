package net.quepierts.simpleanimator.api.animation;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class RequestHolder {
    @Nullable private UUID target;
    @Nullable private ResourceLocation interaction;
    private InteractionState state = InteractionState.IDLE;
    private boolean requester = true;

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
        this.state = InteractionState.IDLE;
        this.target = null;
        this.interaction = null;
    }

    public void success() {
        this.state = InteractionState.RUNNING;
    }

    public void accept(UUID target, ResourceLocation interaction) {
        this.state = InteractionState.RUNNING;
        this.target = target;
        this.interaction = interaction;
        this.requester = false;
    }

    public void invite(UUID target, ResourceLocation interaction) {
        this.target = target;
        this.interaction = interaction;
        this.state = InteractionState.INVITE;
        this.requester = true;
    }

    public InteractionState getState() {
        return state;
    }

    public boolean isRequester() {
        return requester;
    }
}
