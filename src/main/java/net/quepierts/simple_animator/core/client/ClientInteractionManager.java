package net.quepierts.simple_animator.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simple_animator.core.SimpleAnimator;
import net.quepierts.simple_animator.core.common.PlayerUtils;
import net.quepierts.simple_animator.core.common.animation.InteractionManager;
import net.quepierts.simple_animator.core.network.ModNetwork;
import net.quepierts.simple_animator.core.network.packet.InteractAcceptPacket;
import net.quepierts.simple_animator.core.proxy.ClientProxy;

import java.util.UUID;

public class ClientInteractionManager extends InteractionManager {
    private final ClientProxy client;

    public ClientInteractionManager(ClientProxy clientProxy) {
        this.client = clientProxy;
    }

    /*
    * Receive Invite From Other Player
    * */
    @Override
    public boolean invite(Player requester, Player receiver, ResourceLocation location) {
        return super.invite(requester, receiver, location);
    }

    @Override
    public boolean accept(Player requester, Player receiver) {
        if (receiver == Minecraft.getInstance().player) {
            return tryAccept(requester);
        }
        return super.accept(requester, receiver);
    }

    public boolean tryAccept(Player requester) {
        SimpleAnimator.LOGGER.info("Try Accept");
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        UUID uuid = requester.getUUID();

        Request request = this.get(uuid);
        if (request == null)
            return false;

        Vec3 position = PlayerUtils.getRelativePosition(requester, 1, 0);
        if (player.distanceToSqr(position) > 0.01) {
            client.getNavigator().navigateTo(
                    requester, 1, 0,
                    () -> ModNetwork.sendToServer(new InteractAcceptPacket(uuid, Minecraft.getInstance().player.getUUID()))
            );
            return false;
        }

        return super.accept(requester, player);
    }

    public boolean requesting() {
        return this.exist(Minecraft.getInstance().player.getUUID());
    }

    protected Request getLocalRequest() {
        return get(Minecraft.getInstance().player.getUUID());
    }
}
