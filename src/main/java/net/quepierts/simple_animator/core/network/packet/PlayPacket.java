package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.network.ModNetwork;

import java.util.UUID;

public class PlayPacket extends UserPacket {
    public final ResourceLocation animation;
    public PlayPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
        this.animation = byteBuf.readResourceLocation();
    }

    public PlayPacket(UUID uuid, ResourceLocation animation) {
        super(uuid);
        this.animation = animation;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeResourceLocation(this.animation);
    }

    @Override
    public void update(NetworkEvent.Context context) {
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(uuid).play(animation);
        ModNetwork.sendToPlayers(this, context.getSender());
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        SimpleAnimator.LOGGER.info("Handle Sync Play");
        SimpleAnimator.getInstance().getProxy().getAnimatorManager().get(uuid).play(animation);
    }
}
