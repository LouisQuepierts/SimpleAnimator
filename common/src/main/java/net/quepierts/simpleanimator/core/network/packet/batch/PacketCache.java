package net.quepierts.simpleanimator.core.network.packet.batch;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.quepierts.simpleanimator.core.network.IPacket;
import net.quepierts.simpleanimator.core.network.NetworkContext;
import org.jetbrains.annotations.NotNull;

public class PacketCache implements IPacket {
    private final FriendlyByteBuf byteBuf;
    private Type<? extends CustomPacketPayload> type;

    public PacketCache() {
        byteBuf = new FriendlyByteBuf(Unpooled.buffer());
    }

    public void reset(@NotNull IPacket packet) {
        this.type = packet.type();
        this.byteBuf.clear();
        packet.write(this.byteBuf);
    }

    public boolean ready() {
        return this.type != null && this.byteBuf.isReadable();
    }

    @Override
    public void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeBytes(this.byteBuf.duplicate());
    }

    @Override
    public void handle(NetworkContext context) {

    }

    @Override @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return type;
    }
}
