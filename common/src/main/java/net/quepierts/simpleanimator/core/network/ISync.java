package net.quepierts.simpleanimator.core.network;

public interface ISync extends IPacket {
    @Override
    default void handle(NetworkContext context) {
        sync();
    }

    void sync();
}
