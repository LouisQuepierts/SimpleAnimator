package net.quepierts.simpleanimator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;

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
    public void update(ServerPlayer sender) {
        SimpleAnimator.getProxy().getAnimatorManager().get(this.owner).play(animation);
        SimpleAnimator.getNetwork().sendToPlayers(this, sender);
    }

    @Override
    public void sync() {
        SimpleAnimator.LOGGER.info("Handle Sync Play");
        SimpleAnimator.getProxy().getAnimatorManager().get(owner).play(animation);
    }
}
