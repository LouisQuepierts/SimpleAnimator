package net.quepierts.simpleanimator.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.packet.*;
import net.quepierts.simpleanimator.core.network.packet.batch.ClientUpdateAnimationPacket;
import net.quepierts.simpleanimator.core.network.packet.batch.ClientUpdateAnimatorPacket;
import net.quepierts.simpleanimator.core.network.packet.batch.ClientUpdateInteractionPacket;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum NetworkPackets {
    ANIMATOR_UPDATE(AnimatorDataPacket.class, AnimatorDataPacket::new, NetworkDirection.ALL),
    ANIMATOR_PLAY(AnimatorPlayPacket.class, AnimatorPlayPacket::new, NetworkDirection.ALL),
    ANIMATOR_STOP(AnimatorStopPacket.class, AnimatorStopPacket::new, NetworkDirection.ALL),
    INTERACT_INVITE(InteractInvitePacket.class, InteractInvitePacket::new, NetworkDirection.ALL),
    INTERACT_ACCEPT(InteractAcceptPacket.class, InteractAcceptPacket::new, NetworkDirection.ALL),
    INTERACT_CANCEL(InteractCancelPacket.class, InteractCancelPacket::new, NetworkDirection.ALL),
    CLIENT_UPDATE_ANIMATION(ClientUpdateAnimationPacket.class, ClientUpdateAnimationPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    CLIENT_UPDATE_INTERACTION(ClientUpdateInteractionPacket.class, ClientUpdateInteractionPacket::new, NetworkDirection.PLAY_TO_CLIENT),
    CLIENT_UPDATE_ANIMATOR(ClientUpdateAnimatorPacket.class, ClientUpdateAnimatorPacket::new, NetworkDirection.PLAY_TO_CLIENT);
    private final PacketType<?> packet;

    public PacketType<?> getPacket() {
        return packet;
    }

    <T extends IPacket> NetworkPackets(
            Class<T> type,
            Function<FriendlyByteBuf, T> decoder,
            NetworkDirection direction
    ) {
        packet = new PacketType<>(type, decoder, T::handle, direction);
    }

    public static void register() {
        INetwork network = SimpleAnimator.getNetwork();
        for (NetworkPackets value : values()) {
            network.register(value.packet);
        }
    }

    public static class PacketType<T extends IPacket> {
        public final Class<T> type;
        public final BiConsumer<T, FriendlyByteBuf> encoder;
        public final Function<FriendlyByteBuf, T> decoder;
        public final BiConsumer<T, NetworkContext> handler;
        public final NetworkDirection direction;

        public PacketType(
                @NotNull Class<T> type,
                @NotNull Function<FriendlyByteBuf, T> decoder,
                @NotNull BiConsumer<T, NetworkContext> handler,
                @NotNull NetworkDirection direction) {
            this.type = type;
            this.decoder = decoder;
            this.direction = direction;

            this.encoder = T::write;
            this.handler = handler;
        }
    }
}
