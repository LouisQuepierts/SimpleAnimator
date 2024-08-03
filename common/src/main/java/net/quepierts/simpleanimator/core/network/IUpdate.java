package net.quepierts.simpleanimator.core.network;

import net.minecraft.server.level.ServerPlayer;

public interface IUpdate extends IPacket {
    @Override
    default void handle(NetworkContext context) {
        if (context.direction() == NetworkDirection.PLAY_TO_SERVER)
            update(context.sender());
    }

    void update(ServerPlayer player);
}
