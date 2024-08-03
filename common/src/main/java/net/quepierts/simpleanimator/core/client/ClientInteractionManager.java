package net.quepierts.simpleanimator.core.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.quepierts.simpleanimator.core.PlayerUtils;
import net.quepierts.simpleanimator.core.SimpleAnimator;
import net.quepierts.simpleanimator.core.animation.InteractionManager;
import net.quepierts.simpleanimator.core.network.packet.InteractAcceptPacket;

import java.util.UUID;

public class ClientInteractionManager extends InteractionManager {
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
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        UUID uuid = requester.getUUID();

        Request request = this.get(uuid);
        if (request == null)
            return false;

        Vec3 position = PlayerUtils.getRelativePosition(requester, 1, 0);
        if (player.distanceToSqr(position) > 0.01) {
            SimpleAnimator.getClient().getNavigator().navigateTo(
                    requester, 1, 0,
                    () -> SimpleAnimator.getNetwork().update(new InteractAcceptPacket(uuid, Minecraft.getInstance().player.getUUID()))
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
