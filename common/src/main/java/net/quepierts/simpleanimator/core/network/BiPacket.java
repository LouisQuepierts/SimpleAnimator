package net.quepierts.simpleanimator.core.network;

import net.minecraft.server.level.ServerPlayer;

public abstract class BiPacket implements IPacket {
    public void handle(NetworkContext context) {
        switch (context.direction()) {
            case PLAY_TO_CLIENT:
                sync();
                break;
            case PLAY_TO_SERVER:
                if (context.sender() != null)
                    update(context.sender());
                break;
        }
    }

    protected abstract void update(ServerPlayer sender);

    protected abstract void sync();
}
