package net.quepierts.simpleanimator.fabric.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.network.*;
import org.apache.commons.compress.utils.Sets;

import java.util.HashSet;

public class FabricNetworkImpl implements INetwork {
    @Override
    public void sendToPlayer(IPacket packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public void sendToAllPlayers(IPacket packet, ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(serverPlayer, packet);
        }
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
                .forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    @Override
    public void sendToPlayers(IPacket packet, ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            if (serverPlayer == player)
                continue;

            ServerPlayNetworking.send(serverPlayer, packet);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void update(IPacket packet) {
    }

    @Override
    public <T extends IPacket> void register(NetworkPackets.PacketType<T> packet) {
        if (packet.direction != NetworkDirection.PLAY_TO_CLIENT) {
            PayloadTypeRegistry.playC2S().register(packet.type, packet.codec);
            ServerPlayNetworking.registerGlobalReceiver(
                    packet.type,
                    FabricNetworkImpl::handle
            );
        }

        if (packet.direction != NetworkDirection.PLAY_TO_SERVER) {
            PayloadTypeRegistry.playS2C().register(packet.type, packet.codec);
        }
    }

    private static void handle(IPacket payload, ServerPlayNetworking.Context context) {
        payload.handle(new NetworkContext(NetworkDirection.PLAY_TO_SERVER, context.player()));
    }
}
