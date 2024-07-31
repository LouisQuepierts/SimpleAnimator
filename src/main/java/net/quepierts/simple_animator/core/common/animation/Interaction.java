package net.quepierts.simple_animator.core.common.animation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record Interaction(
        @NotNull ResourceLocation invite,
        @NotNull ResourceLocation requester,
        @NotNull ResourceLocation receiver
) {
    public static void toNetwork(FriendlyByteBuf byteBuf, Interaction interaction) {
        byteBuf.writeResourceLocation(interaction.invite);
        byteBuf.writeResourceLocation(interaction.requester);
        byteBuf.writeResourceLocation(interaction.receiver);

    }

    public static Interaction fromNetwork(FriendlyByteBuf byteBuf) {
        final ResourceLocation invite = byteBuf.readResourceLocation();
        final ResourceLocation requester = byteBuf.readResourceLocation();
        final ResourceLocation receiver = byteBuf.readResourceLocation();

        return new Interaction(
                invite,
                requester,
                receiver
        );
    }
}
