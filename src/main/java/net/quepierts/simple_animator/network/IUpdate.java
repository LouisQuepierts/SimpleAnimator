package net.quepierts.simple_animator.network;

import net.minecraftforge.network.NetworkEvent;

public interface IUpdate extends IPacket {
    void update(NetworkEvent.Context context);
}
