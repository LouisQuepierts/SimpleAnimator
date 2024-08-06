package net.quepierts.simpleanimator.fabric.network;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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
    protected final ImmutableMap<Class<? extends IPacket>, PacketType<FabricPacketImpl>> packets;

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
        if (this.notExist(packet))
            return;
        ServerPlayNetworking.send(player, new FabricPacketImpl(packet));
    }

    @Override
    public void sendToAllPlayers(IPacket packet, ServerPlayer player) {
        if (this.notExist(packet))
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
        if (this.notExist(packet))
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
        if (this.notExist(packet))
            return;

        MinecraftServer server = player.getServer();
        if (server == null)
            return;

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            if (serverPlayer == player)
                continue;

            ServerPlayNetworking.send(serverPlayer, new FabricPacketImpl(packet));
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void update(IPacket packet) {
    }

    protected PacketType<FabricPacketImpl> getPacketID(IPacket packet) {
        return this.packets.get(packet.getClass());
    }

    protected boolean notExist(IPacket packet) {
        return !this.packets.containsKey(packet.getClass());
    }

    @SuppressWarnings("all")
    @Override
    public <T extends IPacket> void register(NetworkPackets.PacketType<T> packet) {
        if (packet.direction != NetworkDirection.PLAY_TO_CLIENT) {
            ServerPlayNetworking.registerGlobalReceiver(
                    this.packets.get(packet.type),
                    (fabricPacket, player, responseSender) -> fabricPacket.getPacket().handle(new NetworkContext(NetworkDirection.PLAY_TO_SERVER, player))
            );
        }
    }

    protected final class FabricPacketImpl implements FabricPacket {
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
