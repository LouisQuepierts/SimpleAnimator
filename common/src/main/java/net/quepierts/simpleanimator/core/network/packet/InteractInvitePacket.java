package net.quepierts.simpleanimator.core.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.api.IInteractHandler;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InteractInvitePacket extends UserPacket {
    private final UUID target;
    private final ResourceLocation interaction;

    public InteractInvitePacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);
        this.target= byteBuf.readUUID();
        this.interaction = byteBuf.readResourceLocation();
    }

    public InteractInvitePacket(UUID uuid, UUID target, ResourceLocation interaction) {
        super(uuid);
        this.target = target;
        this.interaction = interaction;
    }

    @Override
    public void write(FriendlyByteBuf byteBuf) {
        super.write(byteBuf);
        byteBuf.writeUUID(this.target);
        byteBuf.writeResourceLocation(interaction);
    }

    @Override
    public void update(@NotNull ServerPlayer sender) {
        Player target = sender.level().getPlayerByUUID(this.target);
        if (target == null)
            return;

        IInteractHandler handler = (IInteractHandler) sender;
        if (handler.simpleanimator$invite(target, this.interaction, false)) {
            SimpleAnimator.getNetwork().sendToAllPlayers(this, sender);
        }
    }

    @Override
    public void sync() {
        SimpleAnimator.LOGGER.info("Invite Sync");
        ClientLevel level = Minecraft.getInstance().level;
        Player requester = level.getPlayerByUUID(this.owner);
        Player target = level.getPlayerByUUID(this.target);

        if (requester == null || target == null)
            return;

        ((IInteractHandler) requester).simpleanimator$invite(target, this.interaction, false);
    }

    public UUID getTarget() {
        return target;
    }

    public ResourceLocation getInteraction() {
        return interaction;
    }
}
