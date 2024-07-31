package net.quepierts.simple_animator.core.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public abstract class BiPacket implements IPacket {
    public void handle(NetworkEvent.Context context) {
        switch (context.getDirection()) {
            case PLAY_TO_CLIENT:
                sync(context);
                break;
            case PLAY_TO_SERVER:
                ServerPlayer sender = context.getSender();
                if (sender != null)
                    update(context, sender);
                break;
        }
    }

    protected abstract void update(NetworkEvent.Context context, ServerPlayer sender);

    protected abstract void sync(NetworkEvent.Context context);
}
