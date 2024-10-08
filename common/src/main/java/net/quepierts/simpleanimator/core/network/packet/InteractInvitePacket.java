package net.quepierts.simpleanimator.core.network.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.network.NetworkPackets;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class InteractInvitePacket extends UserPacket {
    public static final Type<InteractInvitePacket> TYPE = NetworkPackets.createType(InteractInvitePacket.class);
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

        if (SimpleAnimator.getProxy().getInteractionManager().invite(sender, target, this.interaction)) {
            SimpleAnimator.getNetwork().sendToAllPlayers(this, sender);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void sync() {
        ClientLevel level = Minecraft.getInstance().level;
        Player requester = level.getPlayerByUUID(this.owner);
        Player target = level.getPlayerByUUID(this.target);

        if (requester == null || target == null)
            return;

        SimpleAnimator.getProxy().getInteractionManager().invite(requester, target, this.interaction);
    }

    public UUID getTarget() {
        return target;
    }

    public ResourceLocation getInteraction() {
        return interaction;
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
