package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

public class InteractAcceptPacket extends UserPacket {
    public InteractAcceptPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
    }

    public InteractAcceptPacket(UUID uuid) {
        super(uuid);
    }

    @Override
    public void update(NetworkEvent.Context context) {

    }

    @Override
    protected void sync(NetworkEvent.Context context) {

    }
}
