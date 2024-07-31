package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.network.ModNetwork;

import java.util.UUID;

public class AnimatorPlayPacket extends UserPacket {
    public final ResourceLocation animation;
    public AnimatorPlayPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
        this.animation = byteBuf.readResourceLocation();
    }

    public AnimatorPlayPacket(UUID uuid, ResourceLocation animation) {
        super(uuid);
        this.animation = animation;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeResourceLocation(this.animation);
    }

    @Override
    public void update(NetworkEvent.Context context, ServerPlayer sender) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(this.owner).play(animation);
        ModNetwork.sendToPlayers(this, context.getSender());
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.LOGGER.info("Handle Sync Play");
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(owner).play(animation);
    }
}
