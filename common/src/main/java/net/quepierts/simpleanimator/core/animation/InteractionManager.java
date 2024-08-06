package net.quepierts.simpleanimator.core.animation;

import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractionManager {
    protected final Map<UUID, RequestHolder> requests;

    public InteractionManager() {
        requests = new HashMap<>();
    }

    public void reset() {
        for (RequestHolder holder : this.requests.values()) {
            holder.reset();
        }
    }

    @Nullable
    public RequestHolder get(UUID requester) {
        return this.requests.get(requester);
    }

    public boolean exist(UUID requester) {
        return this.requests.containsKey(requester);
    }

    public void cancel(UUID requester) {
        RequestHolder request = get(requester);

        if (request != null) {
            request.reset();
            SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(requester).stop();
        }
    }

    public RequestHolder createIfAbsent(UUID uuid) {
        return this.requests.computeIfAbsent(uuid, RequestHolder::new);
    }

    public record Request(
            UUID target,
            ResourceLocation interaction
    ) {}
}