package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.network.ModNetwork;

import java.util.UUID;

public class StopPacket extends UserPacket {
    public StopPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
    }

    public StopPacket(UUID uuid) {
        super(uuid);
    }

    @Override
    public void update(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(this.uuid).stop();
        ModNetwork.sendToPlayers(this, context.getSender());
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(this.uuid).stop();
    }
}
