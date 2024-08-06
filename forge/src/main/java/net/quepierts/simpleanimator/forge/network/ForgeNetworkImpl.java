package net.quepierts.simpleanimator.forge.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.INetwork;
import net.quepierts.simpleanimator.core.network.IPacket;
import net.quepierts.simpleanimator.core.network.NetworkContext;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import org.apache.commons.compress.utils.Sets;

import java.util.HashSet;

public class ForgeNetworkImpl implements INetwork {
    private final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SimpleAnimator.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private int index = 0;

    @Override
    public void sendToPlayer(IPacket packet, ServerPlayer player) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    @Override
    public void sendToAllPlayers(IPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }

    @Override
    public void sendToPlayersExcept(IPacket packet, ServerPlayer... except) {
        if (except.length == 0)
            return;
        MinecraftServer server = except[0].getServer();
        if (server == null)
            return;
        HashSet<ServerPlayer> players = Sets.newHashSet(except);
        server.getPlayerList().getPlayers().stream()
                .filter(players::contains)
                .forEach(player -> CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT));
    }

    @Override
    public void sendToPlayers(IPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), packet);
    }

    @Override
    public void update(IPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    @Override
    public <T extends IPacket> void register(NetworkPackets.PacketType<T> type) {
        CHANNEL.messageBuilder(type.type, index++, f2s(type.direction))
                .encoder(type.encoder)
                .decoder(type.decoder)
                .consumerNetworkThread((packet, supplier) -> {
                    NetworkEvent.Context context = supplier.get();
                    context.enqueueWork(() -> type.handler.accept(packet, new NetworkContext(s2f(context.getDirection()), context.getSender())));
                    context.setPacketHandled(true);
                })
                .add();
    }

    private static NetworkDirection f2s(net.quepierts.simpleanimator.core.network.NetworkDirection direction) {
        return switch (direction) {
            case ALL -> null;
            case PLAY_TO_SERVER -> NetworkDirection.PLAY_TO_SERVER;
            case PLAY_TO_CLIENT -> NetworkDirection.PLAY_TO_CLIENT;
        };
    }

    private static net.quepierts.simpleanimator.core.network.NetworkDirection s2f(NetworkDirection direction) {
        return switch (direction) {
            case PLAY_TO_SERVER -> net.quepierts.simpleanimator.core.network.NetworkDirection.PLAY_TO_SERVER;
            case PLAY_TO_CLIENT -> net.quepierts.simpleanimator.core.network.NetworkDirection.PLAY_TO_CLIENT;
            default -> null;
        };
    }
}
