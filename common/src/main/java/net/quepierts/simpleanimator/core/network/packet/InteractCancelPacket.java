package net.quepierts.simpleanimator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;

import java.util.UUID;

public class InteractCancelPacket extends UserPacket {

    public InteractCancelPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
    }

    public InteractCancelPacket(UUID uuid) {
        super(uuid);
    }

    @Override
    protected void update(ServerPlayer sender) {
        SimpleAnimator.getProxy().getInteractionManager().cancel(this.owner);
        SimpleAnimator.getNetwork().sendToPlayers(this, sender);
    }

    @Override
    protected void sync() {
        SimpleAnimator.getProxy().getInteractionManager().cancel(this.owner);
    }
}
