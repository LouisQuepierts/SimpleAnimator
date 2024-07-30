package net.quepierts.simple_animator.animation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record Interaction(
        ResourceLocation invite,
        ResourceLocation requester,
        ResourceLocation receiver
) {
    public static void toNetwork(FriendlyByteBuf byteBuf, Interaction interaction) {
        byteBuf.writeOptional(Optional.ofNullable(interaction.invite), FriendlyByteBuf::writeResourceLocation);
        byteBuf.writeOptional(Optional.ofNullable(interaction.requester), FriendlyByteBuf::writeResourceLocation);
        byteBuf.writeOptional(Optional.ofNullable(interaction.receiver), FriendlyByteBuf::writeResourceLocation);

    }

    public static Interaction fromNetwork(FriendlyByteBuf byteBuf) {
        final Optional<ResourceLocation> invite = byteBuf.readOptional(FriendlyByteBuf::readResourceLocation);
        final Optional<ResourceLocation> requester = byteBuf.readOptional(FriendlyByteBuf::readResourceLocation);
        final Optional<ResourceLocation> receiver = byteBuf.readOptional(FriendlyByteBuf::readResourceLocation);

        return new Interaction(
                invite.orElse(null),
                requester.orElse(null),
                receiver.orElse(null)
        );
    }
}
