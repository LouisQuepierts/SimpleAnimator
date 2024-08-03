package net.quepierts.simpleanimator.fabric.network;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.*;
import org.apache.commons.compress.utils.Sets;

import java.util.HashSet;

public class FabricNetworkImpl implements INetwork {
    private final ImmutableMap<Class<? extends IPacket>, ResourceLocation> packets;

    public FabricNetworkImpl() {
        NetworkPackets[] values = NetworkPackets.values();
        ImmutableMap.Builder<Class<? extends IPacket>, ResourceLocation> builder = ImmutableMap.builder();
        for (NetworkPackets value : values) {
            Class<? extends IPacket> type = value.getPacket().type;
            builder.put(type, new ResourceLocation(SimpleAnimator.MOD_ID, type.getSimpleName()));
        }
        packets = builder.build();
    }

    @Override
    public void sendToPlayer(IPacket packet, ServerPlayer player) {
        ResourceLocation id = this.getPacketID(packet);
        if (id == null)
            return;
        ServerPlayNetworking.send(player, id, this.toBuffer(packet));
    }

    @Override
    public void sendToAllPlayers(IPacket packet, ServerPlayer player) {
        ResourceLocation id = this.getPacketID(packet);
        if (id == null)
            return;

        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(serverPlayer, id, this.toBuffer(packet));
        }
    }

    @Override
    public void sendToPlayersExcept(IPacket packet, ServerPlayer... except) {
        ResourceLocation id = this.getPacketID(packet);
        if (id == null)
            return;

        if (except.length == 0)
            return;
        MinecraftServer server = except[0].getServer();
        if (server == null)
            return;
        HashSet<ServerPlayer> players = Sets.newHashSet(except);
        server.getPlayerList().getPlayers().stream()
                .filter(players::contains)
                .forEach(player -> ServerPlayNetworking.send(player, id, this.toBuffer(packet)));
    }

    @Override
    public void sendToPlayers(IPacket packet, ServerPlayer player) {
        ResourceLocation id = this.getPacketID(packet);
        if (id == null)
            return;

        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            if (serverPlayer == player)
                continue;

            ServerPlayNetworking.send(serverPlayer, id, this.toBuffer(packet));
        }
    }

    @Override
    public void update(IPacket packet) {
        ResourceLocation id = this.getPacketID(packet);
        if (id == null)
            return;
        ClientPlayNetworking.send(id, toBuffer(packet));
    }

    private FriendlyByteBuf toBuffer(IPacket packet) {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(byteBuf);
        return byteBuf;
    }

    private ResourceLocation getPacketID(IPacket packet) {
        return this.packets.get(packet.getClass());
    }

    @Override
    public <T extends IPacket> void register(NetworkPackets.PacketType<T> packet) {
        if (packet.direction != NetworkDirection.PLAY_TO_CLIENT) {
            ServerPlayNetworking.registerGlobalReceiver(
                    this.packets.get(packet.type),
                    (server, player, handler, buf, responseSender) -> server.execute(() -> packet.decoder.apply(buf).handle(new NetworkContext(NetworkDirection.PLAY_TO_SERVER, player)))
            );
        }

        if (packet.direction != NetworkDirection.PLAY_TO_SERVER) {
            ClientPlayNetworking.registerGlobalReceiver(
                    this.packets.get(packet.type),
                    (client, handler, buf, responseSender) -> client.execute(() -> packet.decoder.apply(buf).handle(new NetworkContext(NetworkDirection.PLAY_TO_CLIENT, null)))
            );
        }
    }


}
