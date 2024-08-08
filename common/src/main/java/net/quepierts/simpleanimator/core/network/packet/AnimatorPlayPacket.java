package net.quepierts.simpleanimator.core.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class AnimatorPlayPacket extends UserPacket {
    public static final Type<AnimatorPlayPacket> TYPE = NetworkPackets.createType(AnimatorPlayPacket.class);
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
    public void update(@NotNull ServerPlayer sender) {
        SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(this.owner).play(animation);
        SimpleAnimator.getNetwork().sendToPlayers(this, sender);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void sync() {
        SimpleAnimator.getProxy().getAnimatorManager().createIfAbsent(owner).play(animation);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
