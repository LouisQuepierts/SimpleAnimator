package net.quepierts.simple_animator.core.network;

import net.minecraftforge.network.NetworkEvent;

public interface ISync extends IPacket {
    void sync(NetworkEvent.Context context);
}
