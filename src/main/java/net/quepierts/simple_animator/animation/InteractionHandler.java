package net.quepierts.simple_animator.animation;

import com.ibm.icu.impl.Pair;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InteractionHandler {
    private final Map<UUID, Pair<UUID, Interaction>> requests;

    public InteractionHandler() {
        requests = new HashMap<>();
    }

    public void request(UUID requester, UUID receiver, ResourceLocation location) {

    }
}
