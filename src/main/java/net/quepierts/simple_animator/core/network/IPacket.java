package net.quepierts.simple_animator.core.network;

import net.minecraft.network.FriendlyByteBuf;

public interface IPacket {
    void write(FriendlyByteBuf byteBuf);
}
