package net.quepierts.simpleanimator.core.network.packet.batch;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.Interaction;
import net.quepierts.simpleanimator.core.network.ISync;

import java.util.Map;

public class ClientUpdateInteractionPacket implements ISync {
    private final Map<ResourceLocation, Interaction> interactions;

    public ClientUpdateInteractionPacket(Map<ResourceLocation, Interaction> interactions) {
        this.interactions = ImmutableMap.copyOf(interactions);
    }

    public ClientUpdateInteractionPacket(FriendlyByteBuf byteBuf) {
        this.interactions = byteBuf.readMap(FriendlyByteBuf::readResourceLocation, Interaction::fromNetwork);
    }

    @Override
    public void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeMap(
                interactions,
                FriendlyByteBuf::writeResourceLocation,
                Interaction::toNetwork
        );
    }

    @Override
    public void sync() {
        SimpleAnimator.getClient().getAnimationManager().handleUpdateInteractions(this);
    }

    public Map<ResourceLocation, Interaction> getInteractions() {
        return interactions;
    }
}
