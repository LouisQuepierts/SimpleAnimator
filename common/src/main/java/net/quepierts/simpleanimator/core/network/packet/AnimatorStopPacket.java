package net.quepierts.simpleanimator.core.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AnimatorStopPacket extends UserPacket {
    public AnimatorStopPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
    }

    public AnimatorStopPacket(UUID uuid) {
        super(uuid);
    }

    @Override
    public void update(@NotNull ServerPlayer sender) {
        SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(this.owner).stop();
        SimpleAnimator.getNetwork().sendToPlayers(this, sender);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void sync() {
        SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(this.owner).stop();
    }
}
