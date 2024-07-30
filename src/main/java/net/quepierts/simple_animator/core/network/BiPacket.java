package net.quepierts.simple_animator.core.network;

import net.minecraftforge.network.NetworkEvent;

public abstract class BiPacket implements IPacket {
    public void handle(NetworkEvent.Context context) {
        switch (context.getDirection()) {
            case PLAY_TO_CLIENT:
                sync(context);
                break;
            case PLAY_TO_SERVER:
                update(context);
                break;
        }
    }

    protected abstract void update(NetworkEvent.Context context);

    protected abstract void sync(NetworkEvent.Context context);
}
