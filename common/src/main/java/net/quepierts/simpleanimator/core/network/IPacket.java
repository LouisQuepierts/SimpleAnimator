package net.quepierts.simpleanimator.core.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface IPacket extends CustomPacketPayload {
    void write(FriendlyByteBuf byteBuf);

    void handle(NetworkContext context);
}
