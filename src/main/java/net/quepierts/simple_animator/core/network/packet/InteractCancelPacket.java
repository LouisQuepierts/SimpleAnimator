package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.network.ModNetwork;

import java.util.UUID;

public class InteractCancelPacket extends UserPacket {

    public InteractCancelPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
    }

    public InteractCancelPacket(UUID uuid) {
        super(uuid);
    }

    @Override
    protected void update(NetworkEvent.Context context, ServerPlayer sender) {
        SimpleAnimator.getInstance().getProxy().getInteractionManager().cancel(this.owner);
        ModNetwork.sendToPlayers(this, sender);
    }

    @Override
    protected void sync(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getInteractionManager().cancel(this.owner);
    }
}
