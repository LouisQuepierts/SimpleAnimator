package net.quepierts.simple_animator.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public interface IPacket {
    void write(FriendlyByteBuf byteBuf);
}
