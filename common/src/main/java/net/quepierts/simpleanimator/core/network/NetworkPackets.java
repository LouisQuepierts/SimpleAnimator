package net.quepierts.simpleanimator.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.packet.*;
import net.quepierts.simpleanimator.core.network.packet.batch.ClientUpdateAnimationPacket;
import net.quepierts.simpleanimator.core.network.packet.batch.ClientUpdateAnimatorPacket;
import net.quepierts.simpleanimator.core.network.packet.batch.ClientUpdateInteractionPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
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

    public static <T extends IPacket> CustomPacketPayload.Type<T> createType(Class<T> type) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(SimpleAnimator.MOD_ID, type.getSimpleName().toLowerCase(Locale.ROOT));
        return new CustomPacketPayload.Type<>(location);
    }

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
        public final CustomPacketPayload.Type<T> type;
        public final StreamCodec<FriendlyByteBuf, T> codec;
        public final BiConsumer<T, NetworkContext> handler;
        public final NetworkDirection direction;

        public PacketType(
                @NotNull Class<T> type,
                @NotNull Function<FriendlyByteBuf, T> decoder,
                @NotNull BiConsumer<T, NetworkContext> handler,
                @NotNull NetworkDirection direction) {
            this.type = createType(type);
            this.direction = direction;
            this.handler = handler;

            this.codec = StreamCodec.of(
                    // Why neoforge will write two times?????
                    (byteBuf, packet) -> packet.write(byteBuf),
                    decoder::apply
            );
        }
    }
}
