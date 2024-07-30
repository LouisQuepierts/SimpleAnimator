package net.quepierts.simple_animator.network.packet.data;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.SimpleAnimator;
import net.quepierts.simple_animator.animation.Interaction;
import net.quepierts.simple_animator.network.ISync;

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
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimationManager().handleUpdateInteractions(this);
    }

    public Map<ResourceLocation, Interaction> getInteractions() {
        return interactions;
    }
}
