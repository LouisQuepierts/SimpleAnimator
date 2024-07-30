package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.client.ClientInteractionHandler;
import net.quepierts.simple_animator.core.network.ModNetwork;
import net.quepierts.simple_animator.core.proxy.ClientProxy;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class InteractInvitePacket extends UserPacket {
    private final ResourceLocation interaction;

    public InteractInvitePacket(FriendlyByteBuf byteBuf) {
        super(byteBuf);

        this.interaction = byteBuf.readOptional(FriendlyByteBuf::readResourceLocation).orElse(null);
    }

    public InteractInvitePacket(UUID uuid, @Nullable ResourceLocation interaction) {
        super(uuid);
        this.interaction = interaction;
    }

    @Override
    public void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeOptional(Optional.ofNullable(this.interaction), FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public void update(NetworkEvent.Context context) {
        ServerPlayer sender = context.getSender();
        assert sender != null;
        if (sender.getUUID().equals(this.uuid))
            return;

        if (sender.level().getPlayerByUUID(this.uuid) instanceof ServerPlayer player) {
            ModNetwork.sendToPlayer(new InteractInvitePacket(sender.getUUID(), this.interaction), player);
        }
    }

    @Override
    public void sync(NetworkEvent.Context context) {
        ClientProxy proxy = (ClientProxy) SimpleAnimator.getInstance().getProxy();
        ClientInteractionHandler handler = proxy.getInteractionHandler();
        handler.receive(this.uuid, this.interaction);
    }
}
