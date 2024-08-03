package net.quepierts.simpleanimator.core.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;

public interface INetwork {
    String PROTOCOL_VERSION = "1";

    void sendToPlayer(IPacket packet, ServerPlayer player);

    void sendToAllPlayers(IPacket packet, ServerPlayer player);

    void sendToPlayersExcept(IPacket packet, ServerPlayer... except);

    void sendToPlayers(IPacket packet, ServerPlayer player);

    @Environment(EnvType.CLIENT)
    void update(IPacket packet);

    <T extends IPacket> void register(NetworkPackets.PacketType<T> packet);
}
