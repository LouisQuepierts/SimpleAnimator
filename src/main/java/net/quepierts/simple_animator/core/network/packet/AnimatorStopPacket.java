package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.network.ModNetwork;

import java.util.UUID;

public class AnimatorStopPacket extends UserPacket {
    public AnimatorStopPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
    }

    public AnimatorStopPacket(UUID uuid) {
        super(uuid);
    }

    @Override
    public void update(NetworkEvent.Context context, ServerPlayer sender) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(this.owner).stop();
        ModNetwork.sendToPlayers(this, context.getSender());
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(this.owner).stop();
    }
}
