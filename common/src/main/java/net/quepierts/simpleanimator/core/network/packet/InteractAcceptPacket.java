package net.quepierts.simpleanimator.core.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.IInteractHandler;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InteractAcceptPacket extends UserPacket {
    public static final Type<InteractAcceptPacket> TYPE = NetworkPackets.createType(InteractAcceptPacket.class);
    private final UUID target;
    private final boolean forced;

    public InteractAcceptPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
        this.target = byteBuf.readUUID();
        this.forced = byteBuf.readBoolean();
    }

    public InteractAcceptPacket(UUID requester, UUID receiver, boolean forced) {
        super(requester);
        this.target = receiver;
        this.forced = forced;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeUUID(this.target);
        buffer.writeBoolean(this.forced);
    }

    @Override
    public void update(@NotNull ServerPlayer sender) {
        Player requester = sender.level().getPlayerByUUID(this.owner);
        if (requester == null)
            return;

        if (forced || ((IInteractHandler) sender).simpleanimator$accept(requester, false, false)) {
            SimpleAnimator.getNetwork().sendToPlayers(this, sender);
        }
    }

    @SuppressWarnings("all")
    @Override
    protected void sync() {
        ClientLevel level = Minecraft.getInstance().level;
        Player requester = level.getPlayerByUUID(this.owner);
        Player target = level.getPlayerByUUID(this.target);
        if (requester == null || target == null)
            return;
        ((IInteractHandler) target).simpleanimator$accept(requester, false, forced);
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
