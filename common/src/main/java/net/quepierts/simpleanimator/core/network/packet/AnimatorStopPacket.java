package net.quepierts.simpleanimator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;

import java.util.UUID;

public class AnimatorStopPacket extends UserPacket {
    public AnimatorStopPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
    }

    public AnimatorStopPacket(UUID uuid) {
        super(uuid);
    }

    @Override
    public void update(ServerPlayer sender) {
        SimpleAnimator.getProxy().getAnimatorManager().get(this.owner).stop();
        SimpleAnimator.getNetwork().sendToPlayers(this, sender);
    }

    @Override
    public void sync() {
        SimpleAnimator.getProxy().getAnimatorManager().get(this.owner).stop();
    }
}
