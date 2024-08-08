package net.quepierts.simpleanimator.neoforge.network;

import com.google.common.collect.Sets;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.quepierts.simpleanimator.core.network.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.function.Predicate;

public class NeoForgeNetworkImpl implements INetwork {
    @Override
    public void sendToPlayer(IPacket packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    @Override
    public void sendToAllPlayers(IPacket packet, ServerPlayer player) {
        PacketDistributor.sendToAllPlayers(packet);
    }

    @Override
    public void sendToPlayersExcept(IPacket packet, ServerPlayer... except) {
        MinecraftServer server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(), "Cannot send clientbound payloads on the client");
        HashSet<ServerPlayer> players = Sets.newHashSet(except);
        server.getPlayerList().getPlayers().stream()
                .filter(Predicate.not(players::contains))
                .forEach(player -> PacketDistributor.sendToPlayer(player, packet));
    }

    @Override
    public void sendToPlayers(IPacket packet, ServerPlayer target) {
        MinecraftServer server = Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer(), "Cannot send clientbound payloads on the client");

        server.getPlayerList().getPlayers().stream()
                .filter(player -> target != player)
                .forEach(player -> PacketDistributor.sendToPlayer(player, packet));
    }

    @Override
    public void update(IPacket packet) {
        PacketDistributor.sendToServer(packet);
    }

    private final PayloadRegistrar registrar = new PayloadRegistrar(PROTOCOL_VERSION);

    @Override
    public <T extends IPacket> void register(NetworkPackets.PacketType<T> packet) {
        switch (packet.direction) {
            case ALL:
                registrar.playBidirectional(packet.type, packet.codec, NeoForgeNetworkImpl::handleBi);
                break;
            case PLAY_TO_CLIENT:
                registrar.playToClient(packet.type, packet.codec, NeoForgeNetworkImpl::handleClient);
                break;
            case PLAY_TO_SERVER:
                registrar.playToServer(packet.type, packet.codec, NeoForgeNetworkImpl::handleServer);
                break;
        }
    }

    private static void handleBi(IPacket payload, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (context.flow().isClientbound()) {
                        payload.handle(new NetworkContext(NetworkDirection.PLAY_TO_CLIENT, null));
                    } else {
                        payload.handle(new NetworkContext(NetworkDirection.PLAY_TO_SERVER, (ServerPlayer) context.player()));
                    }
                }
        );

    }

    private static void handleClient(IPacket payload, IPayloadContext context) {
        context.enqueueWork(
                () -> payload.handle(new NetworkContext(NetworkDirection.PLAY_TO_CLIENT, null))
        );
    }

    private static void handleServer(IPacket payload, IPayloadContext context) {
        context.enqueueWork(
                () -> payload.handle(new NetworkContext(NetworkDirection.PLAY_TO_SERVER, (ServerPlayer) context.player()))
        );
    }

}
