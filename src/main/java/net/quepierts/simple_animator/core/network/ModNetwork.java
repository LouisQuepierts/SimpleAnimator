package net.quepierts.simple_animator.core.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("simple_animator", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void sendToPlayer(IPacket packet, ServerPlayer player) {
        CHANNEL.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToAllPlayers(IPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }

    public static void sendToPlayers(IPacket packet, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), packet);
    }

    public static void sendToServer(IPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    public static void register() {
        ModPacket.register();
    }

    public static void update(IPacket packet) {
        CHANNEL.sendToServer(packet);
    }
}
