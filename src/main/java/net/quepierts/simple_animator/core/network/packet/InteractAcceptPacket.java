package net.quepierts.simple_animator.core.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.network.ModNetwork;
import net.quepierts.simple_animator.core.proxy.CommonProxy;

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
    public void update(NetworkEvent.Context context, ServerPlayer sender) {
        CommonProxy proxy = SimpleAnimator.getInstance().getProxy();

        ServerPlayer target = (ServerPlayer) sender.serverLevel().getPlayerByUUID(this.target);
        if (target == null)
            return;

        if (proxy.getInteractionManager().accept(sender, target)) {
            ModNetwork.sendToPlayers(this, sender);
        }
    }

    @Override
    protected void sync(NetworkEvent.Context context) {
        ClientLevel level = Minecraft.getInstance().level;
        Player requester = level.getPlayerByUUID(this.owner);
        Player target = level.getPlayerByUUID(this.target);

        SimpleAnimator.LOGGER.info("Accept");
        SimpleAnimator.getInstance().getProxy().getInteractionManager().accept(requester, target);
    }
}
