package net.quepierts.simple_animator.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.network.packet.*;
import net.quepierts.simple_animator.core.network.packet.batch.ClientUpdateAnimationPacket;
import net.quepierts.simple_animator.core.network.packet.batch.ClientUpdateAnimatorPacket;
import net.quepierts.simple_animator.core.network.packet.batch.ClientUpdateInteractionPacket;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public enum ModPacket {
    ANIMATOR_UPDATE(AnimatorDataPacket.class, AnimatorDataPacket::new, AnimatorDataPacket::handle),
    ANIMATOR_PLAY(AnimatorPlayPacket.class, AnimatorPlayPacket::new, AnimatorPlayPacket::handle),
    ANIMATOR_STOP(AnimatorStopPacket.class, AnimatorStopPacket::new, AnimatorStopPacket::handle),
    INTERACT_INVITE(InteractInvitePacket.class, InteractInvitePacket::new, InteractInvitePacket::handle),
    INTERACT_ACCEPT(InteractAcceptPacket.class, InteractAcceptPacket::new, InteractAcceptPacket::handle),
    INTERACT_CANCEL(InteractCancelPacket.class, InteractCancelPacket::new, InteractCancelPacket::handle),
    CLIENT_UPDATE_ANIMATION(ClientUpdateAnimationPacket.class, ClientUpdateAnimationPacket::new),
    CLIENT_UPDATE_INTERACTION(ClientUpdateInteractionPacket.class, ClientUpdateInteractionPacket::new),
    CLIENT_UPDATE_ANIMATOR(ClientUpdateAnimatorPacket.class, ClientUpdateAnimatorPacket::new);
    private final PacketType<?> packet;

    <T extends IPacket> ModPacket(
            Class<T> type,
            Function<FriendlyByteBuf, T> decoder,
            BiConsumer<T, NetworkEvent.Context> handler
    ) {
        packet = new PacketType<>(type, decoder, handler, null);
    }

    <T extends ISync> ModPacket(
            Class<T> type,
            Function<FriendlyByteBuf, T> decoder
    ) {
        packet = new PacketType<>(type, decoder, T::sync, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void register() {
        for (ModPacket value : values()) {
            value.packet.register();
        }
    }

    private static class PacketType<T extends IPacket> {
        private static int index = 0;

        private final Class<T> type;
        private final BiConsumer<T, FriendlyByteBuf> encoder;
        private final Function<FriendlyByteBuf, T> decoder;
        private final BiConsumer<T, Supplier<NetworkEvent.Context>> handler;
        private final NetworkDirection direction;

        public PacketType(
                Class<T> type, 
                Function<FriendlyByteBuf, T> decoder, 
                BiConsumer<T, NetworkEvent.Context> handler,
                NetworkDirection direction) {
            this.type = type;
            this.decoder = decoder;
            this.direction = direction;

            this.encoder = T::write;
            this.handler = (packet, contextSupplier) -> {
                NetworkEvent.Context context = contextSupplier.get();
                SimpleAnimator.LOGGER.info("Handler Packet: {}", packet.getClass().getSimpleName());
                context.enqueueWork(() -> handler.accept(packet, context));
                context.setPacketHandled(true);
            };
        }

        public void register() {
            ModNetwork.CHANNEL.messageBuilder(type, index++, direction)
                    .encoder(encoder)
                    .decoder(decoder)
                    .consumerNetworkThread(handler)
                    .add();
        }
    }
}
