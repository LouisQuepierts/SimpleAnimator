package net.quepierts.simple_animator.core.network;

import net.minecraftforge.network.NetworkEvent;

public interface IUpdate extends IPacket {
    void update(NetworkEvent.Context context);
}
