package net.quepierts.simpleanimator.core.network;

import net.minecraft.network.FriendlyByteBuf;

public interface IPacket {
    void write(FriendlyByteBuf byteBuf);

    void handle(NetworkContext context);
}
