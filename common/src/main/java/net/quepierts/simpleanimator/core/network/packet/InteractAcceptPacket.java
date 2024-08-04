package net.quepierts.simpleanimator.core.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.IInteractHandler;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InteractAcceptPacket extends UserPacket {
    private final UUID target;

    public InteractAcceptPacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
        this.target = byteBuf.readUUID();
    }

    public InteractAcceptPacket(UUID requester, UUID receiver) {
        super(requester);
        this.target = receiver;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        super.write(buffer);
        buffer.writeUUID(this.target);
    }

    @Override
    public void update(@NotNull ServerPlayer sender) {
        Player target = sender.level().getPlayerByUUID(this.target);
        if (target == null)
            return;

        if (((IInteractHandler) target).simpleanimator$accept(sender, false)) {
            SimpleAnimator.getNetwork().sendToPlayers(this, sender);
        }
    }

    @SuppressWarnings("all")
    @Override
    protected void sync() {
        ClientLevel level = Minecraft.getInstance().level;
        Player requester = level.getPlayerByUUID(this.owner);
        Player target = level.getPlayerByUUID(this.target);

        ((IInteractHandler) target).simpleanimator$accept(requester, false);
    }
}
