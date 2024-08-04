package net.quepierts.simpleanimator.fabric.network;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.*;
import org.apache.commons.compress.utils.Sets;

import java.util.HashSet;
import java.util.Locale;

public class FabricNetworkImpl implements INetwork {
    private final ImmutableMap<Class<? extends IPacket>, PacketType<FabricPacketImpl>> packets;

    public FabricNetworkImpl() {
        NetworkPackets[] values = NetworkPackets.values();
        ImmutableMap.Builder<Class<? extends IPacket>, PacketType<FabricPacketImpl>> builder = ImmutableMap.builder();
        for (NetworkPackets value : values) {
            Class<? extends IPacket> type = value.getPacket().type;
            ResourceLocation location = new ResourceLocation(SimpleAnimator.MOD_ID, type.getSimpleName().toLowerCase(Locale.ROOT));
            builder.put(type, PacketType.create(location, byteBuf -> new FabricPacketImpl(value.getPacket().decoder.apply(byteBuf))));
        }
        packets = builder.build();
    }

    @Override
    public void sendToPlayer(IPacket packet, ServerPlayer player) {
        if (!this.hasType(packet))
            return;
        ServerPlayNetworking.send(player, new FabricPacketImpl(packet));
    }

    @Override
    public void sendToAllPlayers(IPacket packet, ServerPlayer player) {
        if (!this.hasType(packet))
            return;

        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(serverPlayer, new FabricPacketImpl(packet));
        }
    }

    @Override
    public void sendToPlayersExcept(IPacket packet, ServerPlayer... except) {
        if (!this.hasType(packet))
            return;

        if (except.length == 0)
            return;
        MinecraftServer server = except[0].getServer();
        if (server == null)
            return;
        HashSet<ServerPlayer> players = Sets.newHashSet(except);
        server.getPlayerList().getPlayers().stream()
                .filter(players::contains)
                .forEach(player -> ServerPlayNetworking.send(player, new FabricPacketImpl(packet)));
    }

    @Override
    public void sendToPlayers(IPacket packet, ServerPlayer player) {
        if (!this.hasType(packet))
            return;

        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            if (serverPlayer == player)
                continue;

            ServerPlayNetworking.send(player, new FabricPacketImpl(packet));
        }
    }

    @Override
    public void update(IPacket packet) {
        PacketType<FabricPacketImpl> type = this.getPacketID(packet);
        if (type == null)
            return;
        ClientPlayNetworking.send(new FabricPacketImpl(packet));
    }

    private PacketType<FabricPacketImpl> getPacketID(IPacket packet) {
        return this.packets.get(packet.getClass());
    }

    private boolean hasType(IPacket packet) {
        return this.packets.containsKey(packet.getClass());
    }

    @Override
    public <T extends IPacket> void register(NetworkPackets.PacketType<T> packet) {
        if (packet.direction != NetworkDirection.PLAY_TO_CLIENT) {
            ServerPlayNetworking.registerGlobalReceiver(
                    this.packets.get(packet.type),
                    (fabricPacket, player, responseSender) -> {
                        fabricPacket.getPacket().handle(new NetworkContext(NetworkDirection.PLAY_TO_SERVER, player));
                    }
            );
        }

        if (packet.direction != NetworkDirection.PLAY_TO_SERVER) {
            ClientPlayNetworking.registerGlobalReceiver(
                    this.packets.get(packet.type),
                    (fabricPacket, player, responseSender) -> {
                        fabricPacket.getPacket().handle(new NetworkContext(NetworkDirection.PLAY_TO_CLIENT, null));
                    }
            );
        }
    }

    private final class FabricPacketImpl implements FabricPacket {
        private final IPacket packet;
        public FabricPacketImpl(IPacket packet) {
            this.packet = packet;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            packet.write(buf);
        }

        @Override
        public PacketType<?> getType() {
            return getPacketID(packet);
        }

        public IPacket getPacket() {
            return packet;
        }
    }
}
