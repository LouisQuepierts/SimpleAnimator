package net.quepierts.simpleanimator.core.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InteractCancelPacket extends UserPacket {
    public static final Type<InteractCancelPacket> TYPE = NetworkPackets.createType(InteractCancelPacket.class);

    public InteractCancelPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
    }

    public InteractCancelPacket(UUID uuid) {
        super(uuid);
    }

    @Override
    protected void update(@NotNull ServerPlayer sender) {
        SimpleAnimator.getProxy().getInteractionManager().cancel(this.owner);
        SimpleAnimator.getNetwork().sendToPlayers(this, sender);
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void sync() {
        SimpleAnimator.getProxy().getInteractionManager().cancel(this.owner);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
