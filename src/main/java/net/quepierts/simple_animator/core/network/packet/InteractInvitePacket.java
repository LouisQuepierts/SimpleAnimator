package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.network.ModNetwork;
import net.quepierts.simple_animator.core.proxy.CommonProxy;

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
    public void update(NetworkEvent.Context context, ServerPlayer sender) {
        CommonProxy proxy = SimpleAnimator.getInstance().getProxy();

        ServerPlayer target = (ServerPlayer) sender.serverLevel().getPlayerByUUID(this.target);
        if (target == null)
            return;

        if (proxy.getInteractionManager().invite(sender, target, this.interaction)) {
            ModNetwork.sendToPlayers(this, sender);
        }
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        ClientLevel level = Minecraft.getInstance().level;
        Player requester = level.getPlayerByUUID(this.owner);
        Player target = level.getPlayerByUUID(this.target);

        if (requester == null || target == null)
            return;

        SimpleAnimator.getInstance().getClient().getClientInteractionHandler().invite(requester, target, this.interaction);
    }

    public UUID getTarget() {
        return target;
    }

    public ResourceLocation getInteraction() {
        return interaction;
    }
}
